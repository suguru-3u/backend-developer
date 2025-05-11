package com.sample.oauth.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class CustomApiAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val redisProvider: RedisProvider,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 特定のURLはこのフィルターをスキップする
        val requestURI = request.requestURI
        if (request.method == "POST" && (requestURI == "/api/login" || requestURI == "/api/user/register")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = CustomHttpServletRequest(request).getAuthenticationTokenFromRequest()
        if (
            redisProvider.existBlackAccessToken(token) ||
            jwtProvider.checkTokenExpirationBeforeNow(token)
        ) throw Exception("アクセストークンが有効ではありません")
        jwtProvider.getUserFromAuthenticationToken(token)?.let {
            SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken(it, null, null)
        }
        filterChain.doFilter(request, response)
    }
}