'use client';

import React, { useState, useEffect } from 'react';
import { catalogService } from '@/services/catalogService';
import { CategoryResponse, ProductResponse, ProductVariantResponse } from '@/types';
import { formatCurrency } from '@/lib/utils';
import { useAuthStore } from '@/store/useAuthStore';
import toast from 'react-hot-toast';

export default function AdminProductsPage() {
  const { role } = useAuthStore();
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [activeTab, setActiveTab] = useState<'active' | 'inactive'>('active');
  const [keyword, setKeyword] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  // Create / Edit Product Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<ProductResponse | null>(null);
  const [prodName, setProdName] = useState('');
  const [prodSlug, setProdSlug] = useState('');
  const [prodSkuPrefix, setProdSkuPrefix] = useState('');
  const [prodDescription, setProdDescription] = useState('');
  const [prodBrand, setProdBrand] = useState('Mộc Hương');
  const [prodBasePrice, setProdBasePrice] = useState<number>(350000);
  const [prodCategoryId, setProdCategoryId] = useState<number>(1);
  const [prodTags, setProdTags] = useState('Best Seller, Tự Nhiên');
  const [selectedFiles, setSelectedFiles] = useState<FileList | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  // Variant Drawer
  const [variantDrawerProduct, setVariantDrawerProduct] = useState<ProductResponse | null>(null);
  const [variantsList, setVariantsList] = useState<ProductVariantResponse[]>([]);
  const [newVariantSku, setNewVariantSku] = useState('');
  const [newVariantName, setNewVariantName] = useState('');
  const [newVariantPrice, setNewVariantPrice] = useState<number>(350000);
  const [newVariantWeight, setNewVariantWeight] = useState<number>(100);
  const [newVariantStock, setNewVariantStock] = useState<number>(20);
  const [isAddingVariant, setIsAddingVariant] = useState(false);

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [cats, prodsPage] = await Promise.all([
        catalogService.getCategories(),
        activeTab === 'active'
          ? catalogService.getProducts({ keyword: keyword.trim() || undefined, size: 50 })
          : catalogService.getInactiveProducts(0, 50),
      ]);
      setCategories(cats);
      setProducts(prodsPage.content || []);
    } catch (e) {
      console.error('Failed to load products/categories:', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [activeTab]);

  const openCreateModal = () => {
    setEditingProduct(null);
    setProdName('');
    setProdSlug('');
    setProdSkuPrefix('MH-TRAM');
    setProdDescription('');
    setProdBrand('Mộc Hương');
    setProdBasePrice(350000);
    setProdCategoryId(categories[0]?.id || 1);
    setProdTags('Best Seller, Tự Nhiên');
    setSelectedFiles(null);
    setIsModalOpen(true);
  };

  const openEditModal = (prod: ProductResponse) => {
    setEditingProduct(prod);
    setProdName(prod.name);
    setProdSlug(prod.slug);
    setProdSkuPrefix(prod.skuPrefix);
    setProdDescription(prod.description);
    setProdBrand(prod.brand);
    setProdBasePrice(prod.basePrice);
    setProdCategoryId(prod.categoryId);
    setProdTags(Array.isArray(prod.tags) ? prod.tags.join(', ') : String(prod.tags || ''));
    setSelectedFiles(null);
    setIsModalOpen(true);
  };

  const handleProductSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!prodName.trim() || !prodSkuPrefix.trim()) {
      toast.error('Vui lòng điền tên sản phẩm và mã SKU');
      return;
    }

    setIsSaving(true);
    const formData = new FormData();

    const productPayload = {
      name: prodName.trim(),
      slug: prodSlug.trim() || prodName.toLowerCase().replace(/[^a-z0-9]+/g, '-'),
      skuPrefix: prodSkuPrefix.trim(),
      description: prodDescription.trim(),
      brand: prodBrand.trim(),
      basePrice: prodBasePrice,
      categoryId: prodCategoryId,
      tags: prodTags.split(',').map((t) => t.trim()),
      isActive: true,
    };

    formData.append(
      'product',
      new Blob([JSON.stringify(productPayload)], { type: 'application/json' })
    );

    if (selectedFiles && selectedFiles.length > 0) {
      for (let i = 0; i < selectedFiles.length; i++) {
        formData.append('images', selectedFiles[i]);
      }
    }

    try {
      if (editingProduct) {
        await catalogService.updateProduct(editingProduct.id, formData);
        toast.success('Đã cập nhật sản phẩm thành công!');
      } else {
        await catalogService.createProduct(formData);
        toast.success('Đã tạo sản phẩm mới thành công!');
      }
      setIsModalOpen(false);
      loadData();
    } catch (err: any) {
      console.error('Save product error:', err);
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra khi lưu sản phẩm');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn ẩn sản phẩm này vào thùng rác?')) return;
    try {
      await catalogService.deleteProduct(id);
      toast.success('Đã chuyển sản phẩm vào thùng rác');
      loadData();
    } catch (e) {
      toast.error('Không thể ẩn sản phẩm');
    }
  };

  const handleRestore = async (id: number) => {
    try {
      await catalogService.restoreProduct(id);
      toast.success('Đã khôi phục sản phẩm hoạt động!');
      loadData();
    } catch (e) {
      toast.error('Không thể khôi phục');
    }
  };

  const handleForceDelete = async (id: number) => {
    if (!confirm('CẢNH BÁO: Hành động này sẽ xóa vĩnh viễn sản phẩm khỏi Database! Tiếp tục?')) return;
    try {
      await catalogService.forceDeleteProduct(id);
      toast.success('Đã xóa vĩnh viễn sản phẩm khỏi hệ thống!');
      loadData();
    } catch (e) {
      toast.error('Không thể xóa vĩnh viễn sản phẩm');
    }
  };

  // Variant Drawer functions
  const openVariantDrawer = async (prod: ProductResponse) => {
    setVariantDrawerProduct(prod);
    setNewVariantSku(`${prod.skuPrefix}-${Date.now().toString().slice(-4)}`);
    setNewVariantName('');
    setNewVariantPrice(prod.basePrice);
    try {
      const vars = await catalogService.getVariants(prod.id);
      setVariantsList(vars);
    } catch (e) {
      setVariantsList(prod.variants || []);
    }
  };

  const handleAddVariant = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!variantDrawerProduct || !newVariantSku.trim() || !newVariantName.trim()) {
      toast.error('Vui lòng nhập SKU và tên biến thể');
      return;
    }
    setIsAddingVariant(true);
    try {
      await catalogService.addVariant(variantDrawerProduct.id, {
        sku: newVariantSku.trim(),
        variantName: newVariantName.trim(),
        price: newVariantPrice,
        weightGrams: newVariantWeight,
        initialStock: newVariantStock,
        attributes: { size: newVariantName.trim() },
      });
      toast.success('Đã thêm biến thể mới thành công!');
      const vars = await catalogService.getVariants(variantDrawerProduct.id);
      setVariantsList(vars);
      setNewVariantName('');
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể tạo biến thể');
    } finally {
      setIsAddingVariant(false);
    }
  };

  const handleDeleteVariant = async (variantId: number) => {
    if (!variantDrawerProduct || !confirm('Xác nhận ẩn biến thể này?')) return;
    try {
      await catalogService.deleteVariant(variantDrawerProduct.id, variantId);
      toast.success('Đã ẩn biến thể');
      const vars = await catalogService.getVariants(variantDrawerProduct.id);
      setVariantsList(vars);
    } catch (e) {
      toast.error('Không thể ẩn biến thể');
    }
  };

  const displayedProducts = products.filter((p) =>
    p.name.toLowerCase().includes(keyword.toLowerCase()) ||
    p.skuPrefix.toLowerCase().includes(keyword.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header & Tabs */}
      <div className="bg-surface rounded-3xl p-6 sm:p-8 border border-border-bamboo shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="font-serif text-2xl lg:text-3xl font-bold text-primary">
              Quản Lý Sản Phẩm & Biến Thể
            </h1>
            <p className="text-xs text-text-wood-muted mt-0.5">
              Thêm mới sản phẩm kèm upload ảnh lên Cloudinary, điều chỉnh đa biến thể và thùng rác
            </p>
          </div>

          <button
            onClick={openCreateModal}
            className="px-5 py-2.5 rounded-xl bg-primary text-secondary-fixed text-xs font-semibold hover:bg-primary-container transition-colors shadow-sm flex items-center gap-2 self-start sm:self-auto"
          >
            <span className="material-symbols-outlined text-[18px]">add_box</span>
            <span>Thêm Sản Phẩm Mới</span>
          </button>
        </div>

        {/* Tab switch & Search */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-t border-border-bamboo pt-3">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setActiveTab('active')}
              className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
                activeTab === 'active'
                  ? 'bg-primary text-secondary-fixed shadow-md'
                  : 'text-text-wood-muted hover:bg-surface-container'
              }`}
            >
              Đang Hoạt Động
            </button>
            <button
              onClick={() => setActiveTab('inactive')}
              className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
                activeTab === 'inactive'
                  ? 'bg-primary text-secondary-fixed shadow-md'
                  : 'text-text-wood-muted hover:bg-surface-container'
              }`}
            >
              Thùng Rác / Đã Ẩn
            </button>
          </div>

          <div className="relative w-full sm:w-64">
            <input
              type="text"
              placeholder="Tìm kiếm sản phẩm..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              className="w-full pl-8 pr-3 py-1.5 rounded-xl border border-border-bamboo bg-surface font-sans text-xs text-primary focus:outline-none focus:border-secondary-fixed"
            />
            <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-text-wood-muted text-[16px]">
              search
            </span>
          </div>
        </div>
      </div>

      {/* Products Table */}
      <div className="bg-surface rounded-3xl border border-border-bamboo shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-bg-warm-sand/80 text-text-wood-muted uppercase font-mono border-b border-border-bamboo">
              <tr>
                <th className="p-4">Ảnh</th>
                <th className="p-4">Tên Sản Phẩm</th>
                <th className="p-4">SKU Prefix</th>
                <th className="p-4">Giá Cơ Sở</th>
                <th className="p-4">Biến Thể</th>
                <th className="p-4">Đánh Giá</th>
                <th className="p-4 text-right">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-bamboo/50">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="p-8 text-center text-text-wood-muted">
                    <span className="material-symbols-outlined text-[32px] text-primary animate-spin">spa</span>
                    <p className="mt-2 font-serif text-primary">Đang tải danh sách sản phẩm...</p>
                  </td>
                </tr>
              ) : displayedProducts.length === 0 ? (
                <tr>
                  <td colSpan={7} className="p-12 text-center text-text-wood-muted">
                    {keyword ? 'Không tìm thấy sản phẩm nào phù hợp.' : 'Chưa có sản phẩm nào trong danh sách.'}
                  </td>
                </tr>
              ) : (
                displayedProducts.map((p) => {
                  const imgUrl =
                    p.images?.find((img) => img.isPrimary)?.imageUrl ||
                    p.images?.[0]?.imageUrl ||
                    'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=200';

                  return (
                    <tr key={p.id} className="hover:bg-surface-container-low transition-colors">
                      <td className="p-4">
                        <div className="w-12 h-12 rounded-xl overflow-hidden bg-bg-warm-sand border border-border-bamboo">
                          {/* eslint-disable-next-line @next/next/no-img-element */}
                          <img src={imgUrl} alt={p.name} className="w-full h-full object-cover" />
                        </div>
                      </td>

                      <td className="p-4 max-w-xs">
                        <span className="font-serif font-bold text-sm text-text-ink-black line-clamp-1">
                          {p.name}
                        </span>
                        <span className="text-[11px] text-text-wood-muted block">{p.brand}</span>
                      </td>

                      <td className="p-4 font-mono font-bold text-primary">{p.skuPrefix}</td>

                      <td className="p-4 font-mono font-bold text-primary text-sm">
                        {formatCurrency(p.basePrice)}
                      </td>

                      <td className="p-4">
                        <button
                          onClick={() => openVariantDrawer(p)}
                          className="px-2.5 py-1 rounded-lg bg-surface-container hover:bg-surface-container-high text-primary font-semibold text-xs border border-border-bamboo"
                        >
                          {p.variants?.length || 0} Biến Thể ↗
                        </button>
                      </td>

                      <td className="p-4">
                        <div className="flex items-center gap-1 text-secondary font-bold">
                          <span className="material-symbols-outlined text-[14px]">star</span>
                          <span>{p.averageRating ? p.averageRating.toFixed(1) : '5.0'}</span>
                        </div>
                      </td>

                      <td className="p-4 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          {activeTab === 'active' ? (
                            <>
                              <button
                                onClick={() => openEditModal(p)}
                                className="p-1.5 rounded-lg text-text-wood-muted hover:text-primary hover:bg-surface-container transition-colors"
                                title="Chỉnh sửa"
                              >
                                <span className="material-symbols-outlined text-[18px]">edit</span>
                              </button>
                              <button
                                onClick={() => handleDelete(p.id)}
                                className="p-1.5 rounded-lg text-text-wood-muted hover:text-status-cinnabar hover:bg-status-cinnabar/10 transition-colors"
                                title="Chuyển vào thùng rác"
                              >
                                <span className="material-symbols-outlined text-[18px]">delete</span>
                              </button>
                            </>
                          ) : (
                            <>
                              <button
                                onClick={() => handleRestore(p.id)}
                                className="px-3 py-1 rounded-lg bg-accent-lotus-jade text-surface text-xs font-semibold hover:opacity-90"
                              >
                                Khôi Phục
                              </button>
                              {role === 'ADMIN' && (
                                <button
                                  onClick={() => handleForceDelete(p.id)}
                                  className="px-3 py-1 rounded-lg bg-status-cinnabar text-surface text-xs font-semibold hover:opacity-90"
                                >
                                  Xóa Vĩnh Viễn
                                </button>
                              )}
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal: Create / Edit Product */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-bg-agarwood-dark/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface w-full max-w-2xl max-h-[90vh] overflow-y-auto rounded-3xl border border-border-bamboo p-6 sm:p-8 shadow-2xl space-y-5">
            <div className="flex items-center justify-between pb-3 border-b border-border-bamboo">
              <h3 className="font-serif text-xl font-bold text-primary">
                {editingProduct ? 'Chỉnh Sửa Sản Phẩm' : 'Tạo Sản Phẩm Mới (Multipart Cloudinary)'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-text-wood-muted p-1">
                <span className="material-symbols-outlined text-[20px]">close</span>
              </button>
            </div>

            <form onSubmit={handleProductSubmit} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                    Tên Sản Phẩm *
                  </label>
                  <input
                    type="text"
                    value={prodName}
                    onChange={(e) => setProdName(e.target.value)}
                    placeholder="VD: Nhang Trầm Hương Rừng Tự Nhiên Cao Cấp"
                    required
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                    Mã Tiền Tố SKU Prefix *
                  </label>
                  <input
                    type="text"
                    value={prodSkuPrefix}
                    onChange={(e) => setProdSkuPrefix(e.target.value.toUpperCase())}
                    placeholder="VD: NHANG-TRAM"
                    required
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs font-mono text-text-ink-black focus:outline-none focus:border-primary"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                    Giá Cơ Sở (VNĐ) *
                  </label>
                  <input
                    type="number"
                    value={prodBasePrice}
                    onChange={(e) => setProdBasePrice(Number(e.target.value))}
                    required
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs font-mono text-text-ink-black focus:outline-none focus:border-primary"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Danh Mục *</label>
                  <select
                    value={prodCategoryId}
                    onChange={(e) => setProdCategoryId(Number(e.target.value))}
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                  >
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Thương Hiệu</label>
                  <input
                    type="text"
                    value={prodBrand}
                    onChange={(e) => setProdBrand(e.target.value)}
                    placeholder="Mộc Hương"
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                  />
                </div>

                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                    Thẻ Phân Loại (Tags, cách nhau dấu phẩy)
                  </label>
                  <input
                    type="text"
                    value={prodTags}
                    onChange={(e) => setProdTags(e.target.value)}
                    placeholder="Best Seller, Trầm Rừng, Tự Nhiên"
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                  />
                </div>

                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                    Mô Tả Sản Phẩm & Ý Nghĩa Phong Thủy
                  </label>
                  <textarea
                    rows={3}
                    value={prodDescription}
                    onChange={(e) => setProdDescription(e.target.value)}
                    placeholder="Mô tả chi tiết nguyên liệu, hương vị..."
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                  />
                </div>

                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                    Hình Ảnh Sản Phẩm (Tải lên Cloudinary)
                  </label>
                  <input
                    type="file"
                    multiple
                    accept="image/*"
                    onChange={(e) => setSelectedFiles(e.target.files)}
                    className="w-full p-2 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-3 border-t border-border-bamboo">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 text-xs text-text-wood-muted hover:bg-surface-container rounded-xl"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={isSaving}
                  className="px-6 py-2.5 bg-primary text-secondary-fixed text-xs font-semibold rounded-xl hover:bg-primary-container disabled:opacity-50"
                >
                  {isSaving ? 'Đang tải lên...' : 'Lưu Sản Phẩm'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Drawer: Variant Management */}
      {variantDrawerProduct && (
        <div className="fixed inset-0 z-50 bg-bg-agarwood-dark/60 backdrop-blur-sm flex justify-end">
          <div className="bg-surface w-full max-w-lg h-full p-6 sm:p-8 flex flex-col justify-between overflow-y-auto animate-in slide-in-from-right duration-200">
            <div className="space-y-6">
              <div className="flex items-center justify-between pb-3 border-b border-border-bamboo">
                <div>
                  <h3 className="font-serif text-lg font-bold text-primary">Quản Lý Biến Thể</h3>
                  <p className="text-xs text-text-wood-muted truncate max-w-xs">{variantDrawerProduct.name}</p>
                </div>
                <button onClick={() => setVariantDrawerProduct(null)} className="text-text-wood-muted p-1">
                  <span className="material-symbols-outlined text-[20px]">close</span>
                </button>
              </div>

              {/* Add Variant Form */}
              <form onSubmit={handleAddVariant} className="bg-bg-warm-sand/60 p-4 rounded-2xl border border-border-bamboo space-y-3">
                <span className="text-xs font-bold text-primary uppercase block">Thêm Biến Thể Bán Hàng</span>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="text-[11px] text-text-wood-muted uppercase">Mã SKU *</label>
                    <input
                      type="text"
                      value={newVariantSku}
                      onChange={(e) => setNewVariantSku(e.target.value.toUpperCase())}
                      required
                      className="w-full p-2 bg-surface rounded-lg border border-border-bamboo font-mono text-xs"
                    />
                  </div>
                  <div>
                    <label className="text-[11px] text-text-wood-muted uppercase">Tên Biến Thể *</label>
                    <input
                      type="text"
                      value={newVariantName}
                      onChange={(e) => setNewVariantName(e.target.value)}
                      placeholder="Hộp 50 nụ / Vòng 8mm"
                      required
                      className="w-full p-2 bg-surface rounded-lg border border-border-bamboo text-xs"
                    />
                  </div>
                  <div>
                    <label className="text-[11px] text-text-wood-muted uppercase">Giá Bán (VNĐ) *</label>
                    <input
                      type="number"
                      value={newVariantPrice}
                      onChange={(e) => setNewVariantPrice(Number(e.target.value))}
                      required
                      className="w-full p-2 bg-surface rounded-lg border border-border-bamboo font-mono text-xs"
                    />
                  </div>
                  <div>
                    <label className="text-[11px] text-text-wood-muted uppercase">Khối Lượng (g)</label>
                    <input
                      type="number"
                      value={newVariantWeight}
                      onChange={(e) => setNewVariantWeight(Number(e.target.value))}
                      className="w-full p-2 bg-surface rounded-lg border border-border-bamboo font-mono text-xs"
                    />
                  </div>
                  <div>
                    <label className="text-[11px] text-text-wood-muted uppercase">Tồn Kho Ban Đầu *</label>
                    <input
                      type="number"
                      value={newVariantStock}
                      onChange={(e) => setNewVariantStock(Number(e.target.value))}
                      required
                      className="w-full p-2 bg-surface rounded-lg border border-border-bamboo font-mono text-xs"
                    />
                  </div>
                </div>
                <button
                  type="submit"
                  disabled={isAddingVariant}
                  className="w-full py-2 rounded-xl bg-primary text-secondary-fixed text-xs font-semibold hover:bg-primary-container disabled:opacity-50"
                >
                  {isAddingVariant ? 'Đang thêm...' : '+ Thêm Biến Thể'}
                </button>
              </form>

              {/* Current Variants List */}
              <div className="space-y-3">
                <span className="text-xs font-bold text-primary uppercase block">Danh Sách Biến Thể Hiện Có:</span>
                <div className="space-y-2">
                  {variantsList.map((v) => (
                    <div
                      key={v.id}
                      className="p-3 rounded-xl bg-surface border border-border-bamboo flex items-center justify-between text-xs"
                    >
                      <div>
                        <span className="font-bold text-primary block">{v.variantName}</span>
                        <span className="font-mono text-text-wood-muted text-[11px]">SKU: {v.sku}</span>
                        <span className="font-mono font-bold text-secondary block mt-0.5">{formatCurrency(v.price)}</span>
                      </div>
                      <button
                        onClick={() => handleDeleteVariant(v.id)}
                        className="p-1 rounded text-text-wood-muted hover:text-status-cinnabar"
                        title="Ẩn biến thể"
                      >
                        <span className="material-symbols-outlined text-[16px]">delete</span>
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="pt-4 border-t border-border-bamboo">
              <button
                onClick={() => setVariantDrawerProduct(null)}
                className="w-full py-2.5 rounded-xl bg-surface-container text-primary font-semibold text-xs hover:bg-surface-container-high"
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
