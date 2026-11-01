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
import com.minhtriet.se3979.orderservice.repository.OrderItemRepository;
import com.minhtriet.se3979.orderservice.repository.OrderRepository;
import com.minhtriet.se3979.orderservice.service.OrderExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

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
                String recipient = stripAccents(String.valueOf(addr.getOrDefault("recipientName", "N/A")));
                String phone = String.valueOf(addr.getOrDefault("phoneNumber", "N/A"));
                String address = stripAccents(String.valueOf(addr.getOrDefault("streetAddress", "")));
                document.add(new Paragraph("Nguoi nhan: " + recipient + " - SDT: " + phone));
                document.add(new Paragraph("Dia chi giao hang: " + address));
            }
            document.add(new Paragraph("\n"));

            // Bảng danh sách sản phẩm
            Table table = new Table(UnitValue.createPercentArray(new float[]{35, 20, 15, 10, 20}))
                    .useAllAvailableWidth();

            table.addHeaderCell(new Cell().add(new Paragraph("San pham").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Phien ban").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Don gia (VND)").setBold().setTextAlignment(TextAlignment.RIGHT)));
            table.addHeaderCell(new Cell().add(new Paragraph("SL").setBold().setTextAlignment(TextAlignment.CENTER)));
            table.addHeaderCell(new Cell().add(new Paragraph("Thanh tien (VND)").setBold().setTextAlignment(TextAlignment.RIGHT)));

            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    String pName = stripAccents(item.getProductName() != null ? item.getProductName() : "San pham");
                    String vName = stripAccents(item.getVariantName() != null ? item.getVariantName() : "-");

                    table.addCell(new Cell().add(new Paragraph(pName)));
                    table.addCell(new Cell().add(new Paragraph(vName)));
                    table.addCell(new Cell().add(new Paragraph(df.format(item.getUnitPrice())).setTextAlignment(TextAlignment.RIGHT)));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity())).setTextAlignment(TextAlignment.CENTER)));
                    table.addCell(new Cell().add(new Paragraph(df.format(item.getSubtotal())).setTextAlignment(TextAlignment.RIGHT)));
                }
            }

            document.add(table);
            document.add(new Paragraph("\n"));

            // Tổng kết tiền tệ
            Paragraph totals = new Paragraph()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .add("Tam tinh: " + df.format(order.getSubtotal()) + " VND\n")
                    .add("Phi van chuyen: " + df.format(order.getShippingFee()) + " VND\n")
                    .add("Giam gia: -" + df.format(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO) + " VND\n")
                    .add(new Paragraph("Tong thanh toan: " + df.format(order.getTotalAmount()) + " VND").setBold().setFontSize(14));
            document.add(totals);

            Paragraph footer = new Paragraph("\nCam on quy khach da tin tuong va dong hanh cung Moc Huong Shop!\nHotline ho tro: 1900 8888 | Website: https://mochuong.vn")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(30);
            document.add(footer);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Lỗi khi tạo hóa đơn PDF", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi tạo hóa đơn PDF: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportOrdersToExcel(LocalDate fromDate, LocalDate toDate) {
        return exportOrdersToExcel(fromDate, toDate, null);
    }

    @Override
    public byte[] exportOrdersToExcel(LocalDate fromDate, LocalDate toDate, String status) {
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime end = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        List<Order> orders;
        if (start != null && end != null) {
            orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        } else {
            orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        if (status != null && !status.isBlank()) {
            orders = orders.stream()
                    .filter(o -> status.equalsIgnoreCase(o.getStatus()))
                    .toList();
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sách đơn hàng");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

            String[] columns = {
                    "Mã đơn hàng", "Mã khách hàng", "Trạng thái", "Phương thức TT", "Trạng thái TT",
                    "Tạm tính", "Phí ship", "Giảm giá", "Tổng tiền", "Mã Coupon", "Ngày tạo"
            };

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

        // 3. Revenue by Date (last 7 days - CHỈ tính đơn thành công / đã thanh toán, không tính đơn hủy)
        LocalDate today = LocalDate.now();
        LocalDateTime sevenDaysAgo = today.minusDays(6).atStartOfDay();
        List<Order> last7DaysOrders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(sevenDaysAgo, LocalDateTime.now());
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<DashboardStatsResponse.RevenueByDateDto> revenueByDate = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            BigDecimal dayAmount = last7DaysOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(d))
                    .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()) && !"RETURNED".equalsIgnoreCase(o.getStatus()))
                    .filter(o -> "PAID".equalsIgnoreCase(o.getPaymentStatus())
                            || "DELIVERED".equalsIgnoreCase(o.getStatus())
                            || "COMPLETED".equalsIgnoreCase(o.getStatus()))
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

    private String stripAccents(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(temp).replaceAll("")
                .replace("Đ", "D").replace("đ", "d");
    }
}
