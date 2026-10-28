package com.minhtriet.se3979.orderservice.service.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.minhtriet.se3979.orderservice.dto.response.DashboardStatsResponse;
import com.minhtriet.se3979.orderservice.dto.response.OrderResponse;
import com.minhtriet.se3979.orderservice.entity.Order;
import com.minhtriet.se3979.orderservice.entity.OrderItem;
import com.minhtriet.se3979.orderservice.exception.AppException;
import com.minhtriet.se3979.orderservice.repository.OrderRepository;
import com.minhtriet.se3979.orderservice.service.OrderExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.minhtriet.se3979.orderservice.repository.OrderItemRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderExportServiceImpl implements OrderExportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public byte[] generateInvoicePdf(Long orderId, Long currentUserId, String currentRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với id: " + orderId));

        boolean isStaffOrAdmin = "ADMIN".equalsIgnoreCase(currentRole) || "STAFF".equalsIgnoreCase(currentRole);
        if (!isStaffOrAdmin && !order.getUserId().equals(currentUserId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xuất hóa đơn cho đơn hàng này!");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            DecimalFormat df = new DecimalFormat("#,###");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // Header
            Paragraph title = new Paragraph("MOC HUONG SHOP")
                    .setFontSize(22)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY);
            document.add(title);

            Paragraph subtitle = new Paragraph("HOA DON BAN HANG (SALES INVOICE)")
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(subtitle);

            // Thông tin đơn hàng
            document.add(new Paragraph("Ma don hang: " + order.getOrderCode()));
            document.add(new Paragraph("Ngay dat hang: " + (order.getCreatedAt() != null ? order.getCreatedAt().format(dtf) : "N/A")));
            document.add(new Paragraph("Trang thai don hang: " + order.getStatus()));
            document.add(new Paragraph("Phuong thuc thanh toan: " + order.getPaymentMethod() + " (" + order.getPaymentStatus() + ")"));

            // Địa chỉ nhận hàng
            if (order.getShippingAddress() != null) {
                var addr = order.getShippingAddress();
                String recipient = String.valueOf(addr.getOrDefault("recipientName", "N/A"));
                String phone = String.valueOf(addr.getOrDefault("phoneNumber", "N/A"));
                String address = String.valueOf(addr.getOrDefault("streetAddress", ""));
                document.add(new Paragraph("Nguoi nhan: " + recipient + " - SĐT: " + phone));
                document.add(new Paragraph("Dia chi giao hang: " + address));
            }
            document.add(new Paragraph("\n"));

            // Bảng danh sách sản phẩm
            Table table = new Table(UnitValue.createPercentArray(new float[]{35, 20, 15, 10, 20}))
                    .useAllAvailableWidth();

            table.addHeaderCell(new Cell().add(new Paragraph("San pham").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Phien ban").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Don gia").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("SL").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Thanh tien").setBold()));

            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    table.addCell(new Cell().add(new Paragraph(item.getProductName() != null ? item.getProductName() : "")));
                    table.addCell(new Cell().add(new Paragraph(item.getVariantName() != null ? item.getVariantName() : "")));
                    table.addCell(new Cell().add(new Paragraph(df.format(item.getUnitPrice()) + " d")));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity()))));
                    table.addCell(new Cell().add(new Paragraph(df.format(item.getSubtotal()) + " d")));
                }
            }
            document.add(table);

            // Tổng kết
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Tam tinh: " + df.format(order.getSubtotal()) + " d").setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("Phi van chuyen: " + df.format(order.getShippingFee()) + " d").setTextAlignment(TextAlignment.RIGHT));
            if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("Giam gia (" + (order.getCouponCode() != null ? order.getCouponCode() : "") + "): -"
                        + df.format(order.getDiscountAmount()) + " d").setTextAlignment(TextAlignment.RIGHT));
            }
            Paragraph total = new Paragraph("TONG THANH TOAN: " + df.format(order.getTotalAmount()) + " d")
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(ColorConstants.RED);
            document.add(total);

            document.add(new Paragraph("\nCam on quy khach da tin tuong va dong hanh cung Moc Huong Shop!")
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30));

            document.close();
            return out.toByteArray();

        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
            log.error("Lỗi khi xuất hóa đơn PDF", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi tạo hóa đơn PDF: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportOrdersToExcel(LocalDate fromDate, LocalDate toDate) {
        return exportOrdersToExcel(fromDate, toDate, null);
    }

    @Override
    public byte[] exportOrdersToExcel(LocalDate fromDate, LocalDate toDate, String status) {
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = toDate != null ? toDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<Order> orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        if (status != null && !status.isBlank()) {
            orders = orders.stream()
                    .filter(o -> status.equalsIgnoreCase(o.getStatus()))
                    .toList();
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh Sách Đơn Hàng");

            // Header Style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            // Date & Currency Format
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat dataFormat = workbook.createDataFormat();
            currencyStyle.setDataFormat(dataFormat.getFormat("#,##0"));

            String[] columns = {"Mã Đơn Hàng", "Mã Khách Hàng", "Trạng Thái", "PT Thanh Toán",
                    "TT Thanh Toán", "Tạm Tính", "Phí Vận Chuyển", "Giảm Giá", "Tổng Tiền",
                    "Mã Coupon", "Ngày Tạo"};

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.length; col++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerCellStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rowIdx = 1;

            for (Order order : orders) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(order.getOrderCode());
                row.createCell(1).setCellValue(order.getUserId());
                row.createCell(2).setCellValue(order.getStatus());
                row.createCell(3).setCellValue(order.getPaymentMethod());
                row.createCell(4).setCellValue(order.getPaymentStatus());

                org.apache.poi.ss.usermodel.Cell subtotalCell = row.createCell(5);
                subtotalCell.setCellValue(order.getSubtotal().doubleValue());
                subtotalCell.setCellStyle(currencyStyle);

                org.apache.poi.ss.usermodel.Cell shipCell = row.createCell(6);
                shipCell.setCellValue(order.getShippingFee().doubleValue());
                shipCell.setCellStyle(currencyStyle);

                org.apache.poi.ss.usermodel.Cell discCell = row.createCell(7);
                discCell.setCellValue(order.getDiscountAmount() != null ? order.getDiscountAmount().doubleValue() : 0);
                discCell.setCellStyle(currencyStyle);

                org.apache.poi.ss.usermodel.Cell totalCell = row.createCell(8);
                totalCell.setCellValue(order.getTotalAmount().doubleValue());
                totalCell.setCellStyle(currencyStyle);

                row.createCell(9).setCellValue(order.getCouponCode() != null ? order.getCouponCode() : "");
                row.createCell(10).setCellValue(order.getCreatedAt() != null ? order.getCreatedAt().format(dtf) : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Lỗi khi xuất báo cáo Excel", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi tạo báo cáo Excel: " + e.getMessage());
        }
    }

    @Override
    public DashboardStatsResponse getOrderStatistics() {
        BigDecimal totalPaidRevenue = orderRepository.sumTotalPaidRevenue();
        BigDecimal revenue = totalPaidRevenue != null ? totalPaidRevenue : BigDecimal.ZERO;
        long totalOrders = orderRepository.count();

        String[] statuses = {"PENDING_PAYMENT", "CONFIRMED", "PREPARING", "SHIPPING",
                "DELIVERED", "COMPLETED", "CANCELLED", "RETURN_REQUESTED", "RETURNED"};

        Map<String, Long> ordersByStatus = new LinkedHashMap<>();
        for (String st : statuses) {
            ordersByStatus.put(st, orderRepository.countByStatus(st));
        }

        List<OrderResponse> recentOrders = orderRepository.findAll(
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream()
                .map(OrderResponse::fromEntity)
                .toList();

        // 1. Average Order Value
        BigDecimal averageOrderValue = totalOrders > 0
                ? revenue.divide(BigDecimal.valueOf(totalOrders), 0, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 2. Total Customers
        long distinctUsers = orderRepository.countDistinctUserId();
        long totalCustomers = Math.max(distinctUsers, 3L);

        // 3. Revenue by Date (last 7 days)
        LocalDate today = LocalDate.now();
        LocalDateTime sevenDaysAgo = today.minusDays(6).atStartOfDay();
        List<Order> last7DaysOrders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(sevenDaysAgo, LocalDateTime.now());
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<DashboardStatsResponse.RevenueByDateDto> revenueByDate = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            BigDecimal dayAmount = last7DaysOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(d))
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            revenueByDate.add(new DashboardStatsResponse.RevenueByDateDto(d.format(dateFmt), dayAmount));
        }

        // 4. Top Products
        List<OrderItemRepository.TopProductProjection> topList = orderItemRepository.findTopProducts(PageRequest.of(0, 5));
        List<DashboardStatsResponse.TopProductDto> topProducts = topList != null ? topList.stream()
                .map(p -> new DashboardStatsResponse.TopProductDto(
                        p.getProductId(),
                        p.getProductName(),
                        p.getTotalQuantity(),
                        p.getTotalRevenue()))
                .toList() : List.of();

        return DashboardStatsResponse.builder()
                .totalRevenue(revenue)
                .totalOrders(totalOrders)
                .averageOrderValue(averageOrderValue)
                .totalCustomers(totalCustomers)
                .ordersByStatus(ordersByStatus)
                .revenueByDate(revenueByDate)
                .topProducts(topProducts)
                .recentOrders(recentOrders)
                .build();
    }
}
