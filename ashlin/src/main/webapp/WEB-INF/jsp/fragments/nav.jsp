<header class="top">
    <a class="brand" href="${empty user ? '/login' : (user.role().name() eq 'PATIENT' ? '/patient' : (user.role().name() eq 'DOCTOR' ? '/doctor' : '/admin'))}">
        <svg viewBox="0 0 32 32" aria-hidden="true"><rect width="32" height="32" rx="8" fill="#1e5c4a"/><path d="M8 24V14a8 8 0 0 1 16 0v10" fill="none" stroke="#f3ecdf" stroke-width="2"/><path d="M16 24V16" stroke="#e7c27a" stroke-width="2"/></svg>
        <span>
            <strong>Ashlin Outpatients</strong>
            <em>Salt Lake, Kolkata</em>
        </span>
    </a>
    <nav>
        <c:if test="${not empty user and user.role().name() eq 'PATIENT'}">
            <a href="/patient">Your visits</a>
            <a href="/patient/doctors">Find a doctor</a>
        </c:if>
        <c:if test="${not empty user and user.role().name() eq 'DOCTOR'}">
            <a href="/doctor">Your clinic</a>
        </c:if>
        <c:if test="${not empty user and user.role().name() eq 'ADMIN'}">
            <a href="/admin">Desk</a>
            <a href="/admin/patients">Patients</a>
            <a href="/admin/doctors">Doctors</a>
            <a href="/admin/appointments">Appointments</a>
        </c:if>
        <c:choose>
            <c:when test="${empty user}">
                <a href="/login">Sign in</a>
                <a class="pill" href="/register">Register</a>
            </c:when>
            <c:otherwise>
                <span class="who"><c:out value="${user.name()}"/></span>
                <form method="post" action="/logout"><button type="submit">Sign out</button></form>
            </c:otherwise>
        </c:choose>
    </nav>
</header>
