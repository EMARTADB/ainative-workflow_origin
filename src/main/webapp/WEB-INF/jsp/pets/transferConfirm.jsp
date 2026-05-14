<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>

<petclinic:layout pageName="owners">
    <h2>Confirm Pet Transfer</h2>

    <p>Please review the transfer details before confirming.</p>

    <table class="table table-striped">
        <tr>
            <th>Pet</th>
            <td><c:out value="${pet.name}"/></td>
        </tr>
        <tr>
            <th>Current Owner</th>
            <td><c:out value="${currentOwner.firstName} ${currentOwner.lastName}"/></td>
        </tr>
        <tr>
            <th>New Owner</th>
            <td><c:out value="${newOwner.firstName} ${newOwner.lastName}"/></td>
        </tr>
    </table>

    <form method="post" action="/pets/${pet.id}/transfer/confirm">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <input type="hidden" name="newOwnerId" value="${newOwnerId}"/>
        <button type="submit" class="btn btn-primary">Confirm Transfer</button>
    </form>

    <a href="/pets/${pet.id}/transfer" class="btn btn-default" style="margin-left: 8px;">Cancel</a>
</petclinic:layout>
