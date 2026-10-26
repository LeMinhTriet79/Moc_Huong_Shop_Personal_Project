-- Seed mẫu Email thông báo chuyên nghiệp cho Mộc Hương Shop

INSERT INTO notification_templates (template_key, channel, event_type, subject_template, body_template, description, is_active, version)
VALUES
(
    'ORDER_CREATED_EMAIL',
    'EMAIL',
    'order.created',
    '🌿 [Mộc Hương Shop] Xác nhận đơn hàng thành công - {{orderCode}}',
    '<!DOCTYPE html><html><body style="font-family: Arial, sans-serif; background-color: #f7f5f0; padding: 20px; color: #333;"><div style="max-width: 600px; margin: auto; background: #fff; padding: 30px; border-radius: 8px; border: 1px solid #e0d8cc;"><h2 style="color: #6b4c2b; text-align: center;">MỘC HƯƠNG SHOP</h2><p style="text-align: center; font-style: italic; color: #8c7355;">Hương Trầm Thanh Tịnh - Tâm An Vạn Sự Lành</p><hr style="border: 0; border-top: 1px solid #e0d8cc; margin: 20px 0;"><p>Kính chào <b>{{fullName}}</b>,</p><p>Cảm ơn bạn đã tin tưởng lựa chọn sản phẩm từ Mộc Hương Shop. Đơn hàng của bạn đã được tiếp nhận thành công!</p><div style="background: #faf8f5; padding: 15px; border-radius: 6px; margin: 20px 0;"><p><b>Mã đơn hàng:</b> {{orderCode}}</p><p><b>Phương thức thanh toán:</b> {{paymentMethod}}</p><p><b>Tổng thanh toán:</b> <span style="color: #b54a2a; font-weight: bold; font-size: 16px;">{{totalAmount}} đ</span></p></div><p>Chúng tôi sẽ nhanh chóng chuẩn bị và gửi hàng sớm nhất đến bạn.</p><p style="margin-top: 30px;">Thân chúc bạn một ngày an lạc và nhiều niềm vui!<br><b>Đội ngũ Mộc Hương Shop</b></p></div></body></html>',
    'Mẫu email gửi khách hàng khi tạo đơn hàng mới thành công',
    1,
    1
),
(
    'ORDER_CANCELLED_EMAIL',
    'EMAIL',
    'order.cancelled',
    '⚠️ [Mộc Hương Shop] Thông báo hủy đơn hàng - {{orderCode}}',
    '<!DOCTYPE html><html><body style="font-family: Arial, sans-serif; background-color: #f7f5f0; padding: 20px; color: #333;"><div style="max-width: 600px; margin: auto; background: #fff; padding: 30px; border-radius: 8px; border: 1px solid #e0d8cc;"><h2 style="color: #6b4c2b; text-align: center;">MỘC HƯƠNG SHOP</h2><hr style="border: 0; border-top: 1px solid #e0d8cc; margin: 20px 0;"><p>Kính chào Quý khách,</p><p>Mộc Hương Shop xin thông báo đơn hàng <b>{{orderCode}}</b> của bạn đã được hủy.</p><div style="background: #fff3f0; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 4px solid #e74c3c;"><p><b>Lý do hủy đơn:</b> {{cancelledReason}}</p><p><b>Người thực hiện hủy:</b> {{cancelledBy}}</p></div><p>Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ hotline hoặc phản hồi email này để được hỗ trợ kịp thời.</p><p style="margin-top: 30px;">Trân trọng,<br><b>Mộc Hương Shop</b></p></div></body></html>',
    'Mẫu email gửi khách hàng khi đơn hàng bị hủy',
    1,
    1
),
(
    'ORDER_STATUS_CHANGED_EMAIL',
    'EMAIL',
    'order.status.changed',
    '📦 [Mộc Hương Shop] Cập nhật trạng thái đơn hàng - {{orderCode}}',
    '<!DOCTYPE html><html><body style="font-family: Arial, sans-serif; background-color: #f7f5f0; padding: 20px; color: #333;"><div style="max-width: 600px; margin: auto; background: #fff; padding: 30px; border-radius: 8px; border: 1px solid #e0d8cc;"><h2 style="color: #6b4c2b; text-align: center;">MỘC HƯƠNG SHOP</h2><hr style="border: 0; border-top: 1px solid #e0d8cc; margin: 20px 0;"><p>Kính chào Quý khách,</p><p>Đơn hàng <b>{{orderCode}}</b> của bạn vừa được cập nhật trạng thái mới:</p><div style="background: #f0f7f4; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 4px solid #27ae60;"><p><b>Trạng thái đơn hàng:</b> <span style="color: #27ae60; font-weight: bold; font-size: 16px;">{{toStatus}}</span></p><p><b>Ghi chú:</b> {{note}}</p></div><p>Cảm ơn bạn đã luôn tin yêu và đồng hành cùng Mộc Hương Shop.</p><p style="margin-top: 30px;">Trân trọng,<br><b>Mộc Hương Shop</b></p></div></body></html>',
    'Mẫu email cập nhật tiến độ vận chuyển đơn hàng',
    1,
    1
),
(
    'VERIFY_EMAIL_TEMPLATE',
    'EMAIL',
    'auth.verify_email',
    '🌱 [Mộc Hương Shop] Xác thực tài khoản của bạn',
    '<!DOCTYPE html><html><body style="font-family: Arial, sans-serif; background-color: #f7f5f0; padding: 20px; color: #333;"><div style="max-width: 600px; margin: auto; background: #fff; padding: 30px; border-radius: 8px; border: 1px solid #e0d8cc;"><h2 style="color: #6b4c2b; text-align: center;">MỘC HƯƠNG SHOP</h2><p style="text-align: center; font-style: italic; color: #8c7355;">Chào mừng bạn gia nhập gia đình Mộc Hương!</p><hr style="border: 0; border-top: 1px solid #e0d8cc; margin: 20px 0;"><p>Kính chào <b>{{fullName}}</b>,</p><p>Cảm ơn bạn đã đăng ký tài khoản tại Mộc Hương Shop. Vui lòng nhấn vào nút bên dưới để xác thực địa chỉ email:</p><div style="text-align: center; margin: 30px 0;"><a href="{{verificationLink}}" style="background-color: #6b4c2b; color: #fff; padding: 12px 28px; text-decoration: none; border-radius: 4px; font-weight: bold;">Xác Thực Tài Khoản</a></div><p style="color: #888; font-size: 13px;">Hoặc dán liên kết sau vào trình duyệt: {{verificationLink}}</p><p style="color: #888; font-size: 13px;">Liên kết này có hiệu lực trong vòng 24 giờ.</p></div></body></html>',
    'Mẫu email kích hoạt tài khoản mới',
    1,
    1
),
(
    'RESET_PASSWORD_TEMPLATE',
    'EMAIL',
    'auth.reset_password',
    '🔑 [Mộc Hương Shop] Yêu cầu đặt lại mật khẩu',
    '<!DOCTYPE html><html><body style="font-family: Arial, sans-serif; background-color: #f7f5f0; padding: 20px; color: #333;"><div style="max-width: 600px; margin: auto; background: #fff; padding: 30px; border-radius: 8px; border: 1px solid #e0d8cc;"><h2 style="color: #6b4c2b; text-align: center;">MỘC HƯƠNG SHOP</h2><hr style="border: 0; border-top: 1px solid #e0d8cc; margin: 20px 0;"><p>Kính chào <b>{{fullName}}</b>,</p><p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản liên kết với email này. Vui lòng bấm vào nút bên dưới để tiến hành đổi mật khẩu mới:</p><div style="text-align: center; margin: 30px 0;"><a href="{{resetLink}}" style="background-color: #b54a2a; color: #fff; padding: 12px 28px; text-decoration: none; border-radius: 4px; font-weight: bold;">Đặt Lại Mật Khẩu</a></div><p style="color: #888; font-size: 13px;">Hoặc sử dụng mã OTP sau: <b style="font-size: 16px; color: #333;">{{token}}</b></p><p style="color: #888; font-size: 13px;">Liên kết và mã có hiệu lực trong vòng 15 phút. Nếu bạn không yêu cầu hành động này, vui lòng bỏ qua email.</p></div></body></html>',
    'Mẫu email đặt lại mật khẩu',
    1,
    1
);
