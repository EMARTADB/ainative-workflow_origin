<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>

<petclinic:layout pageName="owners">
    <jsp:attribute name="customScript">
        <link rel="stylesheet" href="/webjars/flatpickr/4.6.13/dist/flatpickr.min.css">
        <script src="/webjars/flatpickr/4.6.13/dist/flatpickr.js"></script>
        <script>
            flatpickr("#birthDate", {});
        </script>
    </jsp:attribute>
    <jsp:body>
        <h2>
            <c:if test="${pet['new']}">New </c:if> Pet
        </h2>
        <form:form modelAttribute="pet"
                   class="form-horizontal">
            <input type="hidden" name="id" value="${pet.id}"/>
            <div class="form-group has-feedback">
                <div class="form-group">
                    <label class="col-sm-2 control-label">Owner</label>
                    <div class="col-sm-10">
                        <c:out value="${pet.owner.firstName} ${pet.owner.lastName}"/>
                    </div>
                </div>
                <petclinic:inputField label="Name" name="name"/>
                <petclinic:inputField label="Birth Date" name="birthDate"/>
                <div class="control-group">
                    <petclinic:selectField name="type" label="Type " names="${types}" size="5"/>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">Microchip ID</label>
                    <div class="col-sm-10">
                        <form:input class="form-control" path="microchipId" size="15" maxlength="15"/>
                        <span class="help-inline"><form:errors path="microchipId"/></span>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <c:choose>
                        <c:when test="${pet['new']}">
                            <button class="btn btn-primary" type="submit">Add Pet</button>
                        </c:when>
                        <c:otherwise>
                            <button class="btn btn-primary" type="submit">Update Pet</button>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </form:form>

        <c:if test="${not pet['new']}">
            <h3>Pet Photo</h3>
            <c:if test="${not empty photoError}">
                <div class="alert alert-danger"><c:out value="${photoError}"/></div>
            </c:if>
            <form method="post"
                  action="/owners/${pet.owner.id}/pets/${pet.id}/photo"
                  enctype="multipart/form-data"
                  class="form-horizontal"
                  id="photoUploadForm">
                <div class="form-group">
                    <label class="col-sm-2 control-label">Photo</label>
                    <div class="col-sm-10">
                        <input type="file" id="photoFile" name="photo"
                               accept="image/jpeg,image/png"
                               class="form-control"/>
                        <span id="photoSizeError" class="text-danger" style="display:none;">
                            File size must not exceed 2 MB.
                        </span>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button class="btn btn-default" type="submit">Upload Photo</button>
                    </div>
                </div>
            </form>
            <script>
                document.getElementById('photoUploadForm').addEventListener('submit', function(e) {
                    var file = document.getElementById('photoFile').files[0];
                    if (file && file.size > 2 * 1024 * 1024) {
                        e.preventDefault();
                        document.getElementById('photoSizeError').style.display = 'inline';
                    }
                });
            </script>
        </c:if>
    </jsp:body>
</petclinic:layout>
