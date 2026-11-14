<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="/WEB-INF/jsp/fragments/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/nav.jsp" %>
<main class="wrap narrow">
    <h1>Create a patient account</h1>
    <p class="lede">Doctors and the registrar are added from the admin desk. This form is for patients.</p>
    <%@ include file="/WEB-INF/jsp/fragments/banners.jsp" %>
    <form class="panel" method="post" action="/register">
        <label>Full name
            <input name="fullName" value="${form.fullName}" maxlength="80" required>
        </label>
        <label>Email
            <input type="email" name="email" value="${form.email}" maxlength="120" required>
        </label>
        <label>Phone
            <input name="phone" value="${form.phone}" maxlength="20" required>
        </label>
        <label>Date of birth
            <input type="date" name="dateOfBirth" value="${form.dateOfBirth}" required>
        </label>
        <label>Password
            <input type="password" name="password" minlength="8" required>
        </label>
        <button class="solid" type="submit">Create the account</button>
    </form>
</main>
<%@ include file="/WEB-INF/jsp/fragments/foot.jsp" %>
</body>
</html>
