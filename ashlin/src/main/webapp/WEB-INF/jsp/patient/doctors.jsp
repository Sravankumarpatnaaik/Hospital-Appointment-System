<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="/WEB-INF/jsp/fragments/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/nav.jsp" %>
<main class="wrap">
    <h1>Find a doctor</h1>
    <p class="lede">Filter by specialization. Each card opens the hours that are still free.</p>
    <%@ include file="/WEB-INF/jsp/fragments/banners.jsp" %>
    <form class="filters panel" method="get" action="/patient/doctors">
        <label>Specialization
            <select name="specialization">
                <option value="">All clinics</option>
                <c:forEach var="name" items="${specializations}">
                    <option value="${name}" ${name eq chosen ? 'selected' : ''}><c:out value="${name}"/></option>
                </c:forEach>
            </select>
        </label>
        <button class="solid" type="submit">Show this list</button>
    </form>
    <c:if test="${empty doctors}"><p class="empty">No doctor is taking appointments for that clinic.</p></c:if>
    <div class="grid">
        <c:forEach var="doctor" items="${doctors}">
            <article class="card">
                <span><c:out value="${doctor.specialization()}"/></span>
                <strong><c:out value="${doctor.name()}"/></strong>
                <p>Room <c:out value="${doctor.room()}"/></p>
                <p><a href="/patient/doctors/${doctor.id()}">See open hours</a></p>
            </article>
        </c:forEach>
    </div>
</main>
<%@ include file="/WEB-INF/jsp/fragments/foot.jsp" %>
</body>
</html>
