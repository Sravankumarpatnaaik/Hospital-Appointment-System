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
    <h1>Doctors</h1>
    <%@ include file="/WEB-INF/jsp/fragments/banners.jsp" %>
    <p><a class="pill" href="/admin/doctors/new">Add a doctor</a></p>
    <div class="panel scroll">
        <table class="sheet">
            <tr><th>Name</th><th>Clinic</th><th>Room</th><th>Email</th><th></th><th></th></tr>
            <c:forEach var="doctor" items="${doctors}">
                <tr>
                    <td><c:out value="${doctor.name()}"/></td>
                    <td><c:out value="${doctor.specialization()}"/></td>
                    <td><c:out value="${doctor.room()}"/></td>
                    <td><c:out value="${doctor.email()}"/></td>
                    <td><span class="tag ${doctor.active() ? '' : 'off'}">${doctor.active() ? 'On duty' : 'Retired'}</span></td>
                    <td><a href="/admin/doctors/${doctor.id()}">Edit</a></td>
                </tr>
            </c:forEach>
        </table>
    </div>
</main>
<%@ include file="/WEB-INF/jsp/fragments/foot.jsp" %>
</body>
</html>
