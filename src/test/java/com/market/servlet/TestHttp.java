package com.market.servlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

final class TestHttp {
    final Map<String, Object> attributes = new ConcurrentHashMap<>();
    final HttpSession session = mock(HttpSession.class);
    final HttpServletRequest request = mock(HttpServletRequest.class);
    final HttpServletResponse response = mock(HttpServletResponse.class);
    final RequestDispatcher dispatcher = mock(RequestDispatcher.class);
    final StringWriter body = new StringWriter();

    TestHttp(Map<String, String> parameters) throws Exception {
        this(parameters, null);
    }

    TestHttp(Map<String, String> parameters, HttpSession sharedSession) throws Exception {
        when(session.getAttribute(anyString())).thenAnswer(call -> attributes.get(call.getArgument(0)));
        doAnswer(call -> { attributes.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(session).setAttribute(anyString(), any());
        doAnswer(call -> { attributes.remove(call.getArgument(0)); return null; })
                .when(session).removeAttribute(anyString());
        HttpSession activeSession = sharedSession == null ? session : sharedSession;
        when(request.getSession()).thenReturn(activeSession);
        when(request.getSession(anyBoolean())).thenReturn(activeSession);
        when(request.getParameter(anyString())).thenAnswer(call -> parameters.get(call.getArgument(0)));
        when(request.getContextPath()).thenReturn("");
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(response.getWriter()).thenReturn(new PrintWriter(body));
    }

    static void inject(Object servlet, String name, Object value) throws Exception {
        Field field = servlet.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(servlet, value);
    }
}
