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
    <h1>Your visits</h1>
    <p class="lede">Book an hour with a doctor, move it, or cancel it. Cancelling frees that doctor’s hour for someone else.</p>
    <%@ include file="/WEB-INF/jsp/fragments/banners.jsp" %>
    <section class="panel">
        <h2>Find a doctor</h2>
        <form class="filters" method="get" action="/patient/doctors">
            <label>Specialization
                <select name="specialization">
                    <option value="">All clinics</option>
                    <c:forEach var="name" items="${specializations}">
                        <option value="${name}"><c:out value="${name}"/></option>
                    </c:forEach>
                </select>
            </label>
            <button class="solid" type="submit">Show this list</button>
        </form>
    </section>
    <section class="panel">
        <h2>Coming up</h2>
        <c:choose>
            <c:when test="${empty upcoming}">
                <p class="empty">You have no upcoming visit. Choose a clinic above.</p>
            </c:when>
            <c:otherwise>
                <c:forEach var="visit" items="${upcoming}">
                    <article class="visit">
                        <div>
                            <strong><c:out value="${visit.when()}"/></strong>
                            <div><c:out value="${visit.title()}"/></div>
                            <div class="meta"><c:out value="${visit.meta()}"/></div>
                            <p><c:out value="${visit.reason()}"/></p>
                        </div>
                        <div class="actions">
                            <a href="/patient/appointments/${visit.id()}">Move</a>
                            <form method="post" action="/patient/appointments/${visit.id()}/cancel" data-confirm="Cancel this visit and free the hour?">
                                <button type="submit">Cancel</button>
                            </form>
                        </div>
                    </article>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </section>
    <section class="panel">
        <h2>Earlier</h2>
        <c:if test="${empty earlier}"><p class="empty">Nothing earlier than today.</p></c:if>
        <c:forEach var="visit" items="${earlier}">
            <article class="visit">
                <div>
                    <strong><c:out value="${visit.when()}"/></strong>
                    <span class="tag ${visit.booked() ? '' : 'off'}"><c:out value="${visit.status()}"/></span>
                    <div><c:out value="${visit.title()}"/> · <c:out value="${visit.meta()}"/></div>
                    <p class="quiet"><c:out value="${visit.reason()}"/></p>
                </div>
            </article>
        </c:forEach>
    </section>
</main>
<%@ include file="/WEB-INF/jsp/fragments/foot.jsp" %>
</body>
</html>
