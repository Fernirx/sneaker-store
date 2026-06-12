export const STAFF_ROLES = [
  'ROLE_ADMIN',
  'ROLE_SALE',
  'ROLE_WAREHOUSE',
  'ROLE_MARKETING',
  'ROLE_TECHNICIAN',
] as const;

export type StaffRole = (typeof STAFF_ROLES)[number];

export function isStaffRole(roles: string[]): boolean {
  return roles.some(r => (STAFF_ROLES as readonly string[]).includes(r));
}
