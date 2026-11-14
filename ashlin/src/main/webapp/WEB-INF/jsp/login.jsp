<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="/WEB-INF/jsp/fragments/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/nav.jsp" %>
<main class="wrap auth">
    <section>
        <h1>One book for the day clinic.</h1>
        <p class="lede">Patients look up a doctor by specialization, then take an open hour. The desk checks that hour in MySQL before it confirms, so the same doctor cannot be booked twice.</p>
        <div class="panel">
            <h2>Demo accounts</h2>
            <table class="demo">
                <tr><th>Role</th><th>Email</th><th>Password</th></tr>
                <tr><td>Patient</td><td>rima.das@ashlin.clinic</td><td>visit-key</td></tr>
                <tr><td>Doctor</td><td>anil.mehta@ashlin.clinic</td><td>round-key</td></tr>
                <tr><td>Registrar</td><td>registrar@ashlin.clinic</td><td>ledger-key</td></tr>
            </table>
            <p class="quiet">Every seeded doctor uses round-key. Every seeded patient uses visit-key.</p>
        </div>
    </section>
    <section class="panel">
        <h2>Sign in</h2>
        <%@ include file="/WEB-INF/jsp/fragments/banners.jsp" %>
        <form method="post" action="/login">
            <label>Email
                <input type="email" name="email" value="${form.email}" autocomplete="username" required>
            </label>
            <label>Password
                <input type="password" name="password" autocomplete="current-password" required>
            </label>
            <button class="solid" type="submit">Enter the desk</button>
        </form>
        <p>New here? <a href="/register">Register as a patient.</a></p>
    </section>
</main>
<%@ include file="/WEB-INF/jsp/fragments/foot.jsp" %>
</body>
</html>
