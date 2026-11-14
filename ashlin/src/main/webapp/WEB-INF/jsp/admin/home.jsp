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
    <h1>Registrar desk</h1>
    <p class="lede">Add patients and doctors, and correct the appointment book. Deleting a person is refused while their visits are still on record.</p>
    <%@ include file="/WEB-INF/jsp/fragments/banners.jsp" %>
    <section class="grid">
        <a class="card" href="/admin/patients"><span>Patients</span><strong>${stats.patients()}</strong><em>Open the register</em></a>
        <a class="card" href="/admin/doctors"><span>Doctors on duty</span><strong>${stats.doctors()}</strong><em>Specializations and rooms</em></a>
        <a class="card" href="/admin/appointments"><span>Visits still ahead</span><strong>${stats.bookedAhead()}</strong><em>Book, move, cancel</em></a>
    </section>
</main>
<%@ include file="/WEB-INF/jsp/fragments/foot.jsp" %>
</body>
</html>
