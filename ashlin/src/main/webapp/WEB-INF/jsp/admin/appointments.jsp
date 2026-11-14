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
    <h1>Appointments</h1>
    <p class="lede">Booking uses the same SQL check as the patient desk. A taken doctor hour is refused.</p>
    <%@ include file="/WEB-INF/jsp/fragments/banners.jsp" %>
    <p><a class="pill" href="/admin/appointments/new">Book a visit</a></p>
    <div class="panel scroll">
        <c:if test="${empty visits}"><p class="empty">The book is empty.</p></c:if>
        <table class="sheet">
            <tr><th>When</th><th>Who</th><th>Reason</th><th>Status</th><th></th></tr>
            <c:forEach var="visit" items="${visits}">
                <tr>
                    <td><c:out value="${visit.when()}"/></td>
                    <td><c:out value="${visit.title()}"/><div class="meta"><c:out value="${visit.meta()}"/></div></td>
                    <td><c:out value="${visit.reason()}"/></td>
                    <td><span class="tag ${visit.booked() ? '' : 'off'}"><c:out value="${visit.status()}"/></span></td>
                    <td class="actions">
                        <c:if test="${visit.booked()}">
                            <a href="/admin/appointments/${visit.id()}">Move</a>
                            <form method="post" action="/admin/appointments/${visit.id()}/cancel" data-confirm="Cancel this visit?">
                                <button type="submit">Cancel</button>
                            </form>
                        </c:if>
                        <form method="post" action="/admin/appointments/${visit.id()}/delete" data-confirm="Delete this visit from the book?">
                            <button type="submit">Delete</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>
</main>
<%@ include file="/WEB-INF/jsp/fragments/foot.jsp" %>
</body>
</html>
