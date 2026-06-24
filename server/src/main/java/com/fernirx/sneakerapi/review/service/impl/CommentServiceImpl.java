package com.fernirx.sneakerapi.review.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.service.ProductService;
import com.fernirx.sneakerapi.review.dto.request.CreateCommentRequest;
import com.fernirx.sneakerapi.review.dto.request.InternalCommentFilterRequest;
import com.fernirx.sneakerapi.review.dto.request.UpdateCommentRequest;
import com.fernirx.sneakerapi.review.dto.response.CommentInternalResponse;
import com.fernirx.sneakerapi.review.dto.response.CommentResponse;
import com.fernirx.sneakerapi.review.dto.response.ReviewerResponse;
import com.fernirx.sneakerapi.review.entity.ProductComment;
import com.fernirx.sneakerapi.review.mapper.CommentMapper;
import com.fernirx.sneakerapi.review.repository.InternalCommentSpec;
import com.fernirx.sneakerapi.review.repository.ProductCommentRepository;
import com.fernirx.sneakerapi.review.service.CommentService;
import com.fernirx.sneakerapi.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final ProductCommentRepository productCommentRepository;
    private final CommentMapper commentMapper;
    private final ProductService productService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getApprovedCommentTree(String productSlug, Pageable pageable) {
        Product product = productService.findActiveBySlug(productSlug);
        List<ProductComment> all = productCommentRepository.findAllByProductId(product.getId());

        Map<Long, List<ProductComment>> byParentId = all.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        List<ProductComment> roots = all.stream()
                .filter(c -> c.getParent() == null && Boolean.TRUE.equals(c.getApproved()))
                .toList();

        int total = roots.size();
        int start = Math.min((int) pageable.getOffset(), total);
        int end = Math.min(start + pageable.getPageSize(), total);
        List<CommentResponse> content = roots.subList(start, end).stream()
                .map(root -> buildNode(root, byParentId))
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    private CommentResponse buildNode(ProductComment node, Map<Long, List<ProductComment>> byParentId) {
        List<CommentResponse> replies = byParentId.getOrDefault(node.getId(), List.of()).stream()
                .filter(c -> Boolean.TRUE.equals(c.getApproved()))
                .sorted(Comparator.comparing(ProductComment::getCreatedAt))
                .map(child -> buildNode(child, byParentId))
                .toList();
        return commentMapper.toResponse(node, ReviewerResponse.from(node.getUser()), replies);
    }

    @Override
    public CommentResponse createComment(Long userId, CreateCommentRequest request) {
        Product product;
        ProductComment parent = null;
        if (request.parentId() != null) {
            parent = productCommentRepository.findById(request.parentId())
                    .orElseThrow(() -> BusinessException.notFound("label.comment"));
            if (!parent.getProduct().getId().equals(request.productId())) {
                throw BusinessException.bad("label.comment");
            }
            product = parent.getProduct();
        } else {
            product = productService.findEntityById(request.productId());
        }

        ProductComment comment = new ProductComment();
        comment.setProduct(product);
        comment.setUser(entityManager.getReference(User.class, userId));
        comment.setParent(parent);
        comment.setContent(request.content());
        comment.setApproved(true);
        comment = productCommentRepository.save(comment);

        return commentMapper.toResponse(comment, ReviewerResponse.from(comment.getUser()), List.of());
    }

    @Override
    public CommentResponse updateComment(Long userId, Long commentId, UpdateCommentRequest request) {
        ProductComment comment = findOwnedComment(userId, commentId);
        commentMapper.updateComment(request, comment);
        comment = productCommentRepository.save(comment);
        return commentMapper.toResponse(comment, ReviewerResponse.from(comment.getUser()), List.of());
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        ProductComment comment = findOwnedComment(userId, commentId);
        productCommentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentInternalResponse> getAll(InternalCommentFilterRequest filter, Pageable pageable) {
        return productCommentRepository.findAll(InternalCommentSpec.build(filter), pageable)
                .map(commentMapper::toInternalResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentInternalResponse getById(Long id) {
        return commentMapper.toInternalResponse(findById(id));
    }

    @Override
    public CommentInternalResponse setApproved(Long id, boolean approved) {
        ProductComment comment = findById(id);
        comment.setApproved(approved);
        return commentMapper.toInternalResponse(productCommentRepository.save(comment));
    }

    @Override
    public void delete(Long id) {
        productCommentRepository.delete(findById(id));
    }

    // ---- Helpers ----

    private ProductComment findOwnedComment(Long userId, Long commentId) {
        ProductComment comment = findById(commentId);
        if (!comment.getUser().getId().equals(userId)) {
            throw SecurityCustomException.forbidden();
        }
        return comment;
    }

    private ProductComment findById(Long id) {
        return productCommentRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.comment"));
    }
}
