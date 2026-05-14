<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>

<petclinic:layout pageName="owners">
    <h2>Transfer Pet Ownership</h2>

    <c:if test="${not empty transferError}">
        <div class="alert alert-danger"><c:out value="${transferError}"/></div>
    </c:if>

    <table class="table table-striped">
        <tr>
            <th>Pet</th>
            <td><c:out value="${pet.name}"/></td>
        </tr>
        <tr>
            <th>Current Owner</th>
            <td><c:out value="${currentOwner.firstName} ${currentOwner.lastName}"/></td>
        </tr>
    </table>

    <h3>Search for New Owner</h3>
    <form method="post" action="/pets/${pet.id}/transfer" class="form-inline">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <div class="form-group">
            <label for="lastName">Last Name:</label>
            <input type="text" id="lastName" name="lastName" class="form-control" value="${fn:escapeXml(searchLastName)}"/>
        </div>
        <button type="submit" class="btn btn-default">Search</button>
    </form>

    <c:if test="${not empty ownerResults}">
        <h4>Search Results</h4>
        <form method="post" action="/pets/${pet.id}/transfer">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <input type="hidden" name="lastName" value=""/>
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Select</th>
                        <th>Name</th>
                        <th>Address</th>
                        <th>City</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="owner" items="${ownerResults}">
                        <tr>
                            <td>
                                <input type="radio" name="newOwnerId" value="${owner.id}"/>
                            </td>
                            <td><c:out value="${owner.firstName} ${owner.lastName}"/></td>
                            <td><c:out value="${owner.address}"/></td>
                            <td><c:out value="${owner.city}"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
            <button type="submit" class="btn btn-primary">Proceed to Confirmation</button>
        </form>
    </c:if>

    <c:if test="${empty ownerResults and not empty searchLastName}">
        <p>No owners found matching "<c:out value="${searchLastName}"/>".</p>
    </c:if>

    <a href="/owners/${currentOwner.id}" class="btn btn-default">Cancel</a>
</petclinic:layout>
