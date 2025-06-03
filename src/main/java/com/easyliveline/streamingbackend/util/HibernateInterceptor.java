package com.easyliveline.streamingbackend.util;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Service;

@Service
public class HibernateInterceptor implements StatementInspector {

    @Override
    public String inspect(String sql) {
        System.out.println("Query From Hibernate Interceptor: ");
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//
//        if (attributes == null) {
//            // Allow Hibernate to continue without modifying the SQL when no request context exists
//            return sql;
//        }

//        HttpServletRequest request = attributes.getRequest();
//        String requestedPath = request.getRequestURI();
//
//        String tenantID = requestedPath.equals("/auth/login")
//                ? TenantUtil.resolveTenantFromRequest()
//                : JwtUtil.getTenantFromJWT();
//
//        if (tenantID != null) {
//            sql = sql.replaceAll("royal", tenantID);
//        }

        return sql;
    }
}
