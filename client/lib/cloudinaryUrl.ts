const CLOUD = process.env.NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME;

function base(publicId: string, transform: string) {
  return `https://res.cloudinary.com/${CLOUD}/image/upload/${transform}/${publicId}`;
}

export function avatarUrl(publicId: string, size = 160) {
  return base(publicId, `w_${size},h_${size},c_fill,g_face,q_auto,f_auto`);
}

export function productUrl(publicId: string, w = 600, h = 600) {
  return base(publicId, `w_${w},h_${h},c_pad,b_white,q_auto,f_auto`);
}

export function brandUrl(publicId: string, w = 200, h = 100) {
  return base(publicId, `w_${w},h_${h},c_fit,q_auto,f_auto`);
}

export function collectionUrl(publicId: string, w = 800, h = 500) {
  return base(publicId, `w_${w},h_${h},c_fill,q_auto,f_auto`);
}

export function categoryUrl(publicId: string, w = 400, h = 400) {
  return base(publicId, `w_${w},h_${h},c_fill,q_auto,f_auto`);
}
