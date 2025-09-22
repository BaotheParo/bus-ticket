package com.long_bus_distance.tickets.repository;

import com.long_bus_distance.tickets.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// Lớp UserProvisioningFilter là một bộ lọc (filter) để tự động tạo người dùng trong cơ sở dữ liệu khi họ đăng nhập lần đầu qua JWT.
// Sử dụng @Component để Spring quản lý như một bean.
// Kế thừa OncePerRequestFilter để đảm bảo bộ lọc chỉ thực thi một lần cho mỗi yêu cầu HTTP.
// @RequiredArgsConstructor tạo constructor tự động cho các trường final (như userRepository).
@Component
@RequiredArgsConstructor
public class UserProvisioningFilter extends OncePerRequestFilter {
    private final UserRepository userRepository; // Repository để thao tác với entity User trong cơ sở dữ liệu

    // Phương thức chính của bộ lọc, được gọi cho mỗi yêu cầu HTTP.
    // @param request Yêu cầu HTTP từ client.
    // @param response Phản hồi HTTP gửi về client.
    // @param filterChain Chuỗi bộ lọc để tiếp tục xử lý yêu cầu.
    // @throws ServletException Nếu có lỗi liên quan đến servlet.
    // @throws IOException Nếu có lỗi nhập/xuất dữ liệu.
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Lấy thông tin xác thực từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra xem xác thực có tồn tại, đã được xác thực và principal là JWT
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt) {
            // Trích xuất ID người dùng (subject) từ JWT và chuyển thành UUID
            UUID keycloakId = UUID.fromString(jwt.getSubject());
            // Kiểm tra xem người dùng có tồn tại trong cơ sở dữ liệu hay không
            if (!userRepository.existsById(keycloakId)) {
                // Nếu người dùng chưa tồn tại, tạo mới một entity User
                User user = new User();
                user.setId(keycloakId); // Gán ID từ JWT (lý do tạm thời xóa @Builder khỏi entity User)
                user.setUsername(jwt.getClaims().get("preferred_username").toString());
                user.setEmail(jwt.getClaims().get("email").toString());
                // Gán giá trị mặc định cho firstname (tên) từ claim "given_name" hoặc "Unknown" nếu null
                user.setFirstname(jwt.getClaims().get("given_name") != null
                        ? jwt.getClaims().get("given_name").toString()
                        : "Unknown");
                user.setLastname(jwt.getClaims().get("family_name") != null
                        ? jwt.getClaims().get("family_name").toString()
                        : "Unknown");
                // Lưu người dùng mới vào cơ sở dữ liệu
                userRepository.save(user);
            }
        }
        // Tiếp tục chuỗi bộ lọc để xử lý yêu cầu tiếp theo
        filterChain.doFilter(request, response);
    }
}