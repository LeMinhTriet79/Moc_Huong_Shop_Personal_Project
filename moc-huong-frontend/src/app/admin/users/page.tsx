'use client';

import React, { useState, useEffect } from 'react';
import { identityService } from '@/services/identityService';
import { UserResponse, UserRole } from '@/types';
import { formatDate } from '@/lib/utils';
import { useAuthStore } from '@/store/useAuthStore';
import toast from 'react-hot-toast';

export default function AdminUsersPage() {
  const { user: currentUser } = useAuthStore();
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [keyword, setKeyword] = useState('');
  const [selectedRole, setSelectedRole] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  // Role Edit Modal
  const [editingUser, setEditingUser] = useState<UserResponse | null>(null);
  const [newRole, setNewRole] = useState<UserRole>('CUSTOMER');
  const [isUpdatingRole, setIsUpdatingRole] = useState(false);

  const loadUsers = async () => {
    setIsLoading(true);
    try {
      const res = await identityService.getAdminUsers({
        keyword: keyword.trim() || undefined,
        role: selectedRole || undefined,
        size: 50,
      });
      setUsers(res.content || []);
    } catch (e) {
      console.error('Failed to load users:', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, [selectedRole]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadUsers();
  };

  const openRoleModal = (u: UserResponse) => {
    if (u.id === currentUser?.id) {
      toast.error('Bạn không thể tự thay đổi vai trò của chính mình!');
      return;
    }
    setEditingUser(u);
    setNewRole(u.role);
  };

  const handleRoleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingUser) return;
    setIsUpdatingRole(true);
    try {
      await identityService.updateUserRole(editingUser.id, newRole);
      toast.success(`Đã cập nhật vai trò của ${editingUser.fullName} thành ${newRole}!`);
      setEditingUser(null);
      loadUsers();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể đổi vai trò');
    } finally {
      setIsUpdatingRole(false);
    }
  };

  const handleToggleStatus = async (u: UserResponse) => {
    if (u.id === currentUser?.id) {
      toast.error('Bạn không thể tự khóa tài khoản của chính mình!');
      return;
    }
    const newStatus = !u.isActive;
    const confirmMsg = newStatus
      ? `Mở khóa tài khoản ${u.email}?`
      : `Khóa tài khoản ${u.email}? Người dùng sẽ bị thu hồi toàn bộ phiên đăng nhập.`;
    if (!confirm(confirmMsg)) return;

    try {
      await identityService.updateUserStatus(u.id, newStatus);
      toast.success(newStatus ? 'Đã mở khóa tài khoản!' : 'Đã khóa tài khoản thành công!');
      loadUsers();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể cập nhật trạng thái');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header & Search */}
      <div className="bg-surface rounded-3xl p-6 sm:p-8 border border-border-bamboo shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <span className="material-symbols-outlined text-secondary text-[20px]">manage_accounts</span>
              <span className="text-xs uppercase font-mono font-bold text-secondary">
                RBAC ACCESS CONTROL • EXCLUSIVE ADMIN
              </span>
            </div>
            <h1 className="font-serif text-2xl lg:text-3xl font-bold text-primary">
              Người Dùng & Phân Quyền Hệ Thống
            </h1>
            <p className="text-xs text-text-wood-muted mt-0.5">
              Tra cứu hồ sơ khách hàng, phân quyền nhân viên vận hành (STAFF) và khóa tài khoản vi phạm
            </p>
          </div>

          <form onSubmit={handleSearchSubmit} className="relative w-full sm:w-64">
            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-text-wood-muted text-[18px]">
              search
            </span>
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="Tìm theo email, tên, SĐT..."
              className="w-full pl-9 pr-3 py-2 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
            />
          </form>
        </div>

        {/* Role Filters */}
        <div className="flex items-center gap-2 border-t border-border-bamboo pt-3">
          {[
            { label: 'Tất Cả', value: '' },
            { label: 'Khách Hàng (CUSTOMER)', value: 'CUSTOMER' },
            { label: 'Nhân Viên (STAFF)', value: 'STAFF' },
            { label: 'Quản Trị Viên (ADMIN)', value: 'ADMIN' },
          ].map((r) => (
            <button
              key={r.value}
              onClick={() => setSelectedRole(r.value)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
                selectedRole === r.value
                  ? 'bg-primary text-secondary-fixed shadow-sm'
                  : 'text-text-wood-muted hover:bg-surface-container'
              }`}
            >
              {r.label}
            </button>
          ))}
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-surface rounded-3xl border border-border-bamboo shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-bg-warm-sand/80 text-text-wood-muted uppercase font-mono border-b border-border-bamboo">
              <tr>
                <th className="p-4">ID</th>
                <th className="p-4">Người Dùng</th>
                <th className="p-4">Email</th>
                <th className="p-4">Số Điện Thoại</th>
                <th className="p-4">Vai Trò (Role)</th>
                <th className="p-4">Trạng Thái</th>
                <th className="p-4">Ngày Tạo</th>
                <th className="p-4 text-right">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-bamboo/50">
              {isLoading ? (
                <tr>
                  <td colSpan={8} className="p-8 text-center text-text-wood-muted">
                    <span className="material-symbols-outlined text-[32px] text-primary animate-spin">spa</span>
                    <p className="mt-2 font-serif text-primary">Đang tải danh sách người dùng...</p>
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan={8} className="p-12 text-center text-text-wood-muted">
                    Không tìm thấy người dùng nào.
                  </td>
                </tr>
              ) : (
                users.map((u) => (
                  <tr key={u.id} className="hover:bg-surface-container-low transition-colors">
                    <td className="p-4 font-mono font-bold text-text-wood-muted">#{u.id}</td>

                    <td className="p-4">
                      <div className="flex items-center gap-2">
                        <div className="w-8 h-8 rounded-full bg-primary text-secondary-fixed flex items-center justify-center font-bold text-xs shrink-0">
                          {u.avatarUrl ? (
                            // eslint-disable-next-line @next/next/no-img-element
                            <img src={u.avatarUrl} alt="" className="w-full h-full object-cover rounded-full" />
                          ) : (
                            u.fullName?.charAt(0) || 'U'
                          )}
                        </div>
                        <div>
                          <span className="font-bold text-text-ink-black block">{u.fullName}</span>
                          <span className="text-[10px] text-text-wood-muted font-mono">{u.provider}</span>
                        </div>
                      </div>
                    </td>

                    <td className="p-4 font-mono text-text-ink-black">{u.email}</td>

                    <td className="p-4 font-mono text-text-wood-muted">{u.phoneNumber || '—'}</td>

                    <td className="p-4">
                      <span
                        className={`px-2.5 py-1 rounded-full text-[10px] font-mono font-bold uppercase ${
                          u.role === 'ADMIN'
                            ? 'bg-secondary-fixed text-primary'
                            : u.role === 'STAFF'
                            ? 'bg-primary-fixed text-primary'
                            : 'bg-surface-container text-text-wood-muted'
                        }`}
                      >
                        {u.role}
                      </span>
                    </td>

                    <td className="p-4">
                      {u.isActive ? (
                        <span className="px-2 py-0.5 rounded-full bg-accent-lotus-jade/15 text-accent-lotus-jade text-[10px] font-bold">
                          Hoạt Động
                        </span>
                      ) : (
                        <span className="px-2 py-0.5 rounded-full bg-status-cinnabar/15 text-status-cinnabar text-[10px] font-bold">
                          Đã Khóa
                        </span>
                      )}
                    </td>

                    <td className="p-4 text-[11px] text-text-wood-muted">{formatDate(u.createdAt)}</td>

                    <td className="p-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => openRoleModal(u)}
                          className="px-2.5 py-1 rounded-lg bg-surface border border-border-bamboo text-primary font-semibold text-[11px] hover:bg-surface-container transition-colors"
                        >
                          Đổi Role
                        </button>
                        <button
                          onClick={() => handleToggleStatus(u)}
                          className={`p-1 rounded-lg border text-[11px] ${
                            u.isActive
                              ? 'border-status-cinnabar/40 text-status-cinnabar hover:bg-status-cinnabar/10'
                              : 'border-accent-lotus-jade/40 text-accent-lotus-jade hover:bg-accent-lotus-jade/10'
                          }`}
                          title={u.isActive ? 'Khóa tài khoản' : 'Mở khóa'}
                        >
                          <span className="material-symbols-outlined text-[16px]">
                            {u.isActive ? 'lock' : 'lock_open'}
                          </span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Role Change Modal */}
      {editingUser && (
        <div className="fixed inset-0 z-50 bg-bg-agarwood-dark/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface w-full max-w-sm rounded-3xl border border-border-bamboo p-6 shadow-2xl space-y-4">
            <h3 className="font-serif text-lg font-bold text-primary">Phân Quyền Cho {editingUser.fullName}</h3>
            <p className="text-xs text-text-wood-muted">{editingUser.email}</p>

            <form onSubmit={handleRoleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                  Chọn Vai Trò (Role)
                </label>
                <select
                  value={newRole}
                  onChange={(e) => setNewRole(e.target.value as any)}
                  className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs font-semibold text-text-ink-black focus:outline-none focus:border-primary"
                >
                  <option value="CUSTOMER">CUSTOMER — Khách hàng mua sắm</option>
                  <option value="STAFF">STAFF — Nhân viên vận hành cửa hàng</option>
                  <option value="ADMIN">ADMIN — Quản trị viên cao cấp</option>
                </select>
              </div>

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setEditingUser(null)}
                  className="px-4 py-2 text-xs text-text-wood-muted hover:bg-surface-container rounded-xl"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={isUpdatingRole}
                  className="px-5 py-2.5 bg-primary text-secondary-fixed text-xs font-bold rounded-xl hover:bg-primary-container disabled:opacity-50"
                >
                  {isUpdatingRole ? 'Đang lưu...' : 'Lưu Quyền'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
