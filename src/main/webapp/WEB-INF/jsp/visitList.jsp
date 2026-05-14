<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>

<petclinic:layout pageName="owners">
    <h2>Visits</h2>

    <table class="table table-striped">
        <thead>
        <tr>
            <th scope="col">Date</th>
            <th scope="col">Description</th>
            <th scope="col">Veterinarian</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="visit" items="${visits}">
            <tr>
                <td><petclinic:localDate date="${visit.date}" pattern="yyyy/MM/dd"/></td>
                <td><c:out value="${visit.description}"/></td>
                <td>
                    <c:choose>
                        <c:when test="${visit.vet != null}">
                            <c:out value="${visit.vet.firstName} ${visit.vet.lastName}"/>
                        </c:when>
                        <c:otherwise></c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</petclinic:layout>
