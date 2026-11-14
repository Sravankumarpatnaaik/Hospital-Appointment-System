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
    <h1>Patients</h1>
    <%@ include file="/WEB-INF/jsp/fragments/banners.jsp" %>
    <p><a class="pill" href="/admin/patients/new">Add a patient</a></p>
    <div class="panel scroll">
        <c:if test="${empty patients}"><p class="empty">No patients yet.</p></c:if>
        <table class="sheet">
            <tr><th>Name</th><th>Email</th><th>Phone</th><th>Born</th><th></th></tr>
            <c:forEach var="patient" items="${patients}">
                <tr>
                    <td><c:out value="${patient.fullName()}"/></td>
                    <td><c:out value="${patient.email()}"/></td>
                    <td><c:out value="${patient.phone()}"/></td>
                    <td><c:out value="${patient.dateOfBirth()}"/></td>
                    <td><a href="/admin/patients/${patient.id()}">Edit</a></td>
                </tr>
            </c:forEach>
        </table>
    </div>
</main>
<%@ include file="/WEB-INF/jsp/fragments/foot.jsp" %>
</body>
</html>
