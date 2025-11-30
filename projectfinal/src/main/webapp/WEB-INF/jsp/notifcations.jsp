<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="layout/header.jsp">
    <jsp:param name="title" value="Notifications - Système de Gestion RH" />
    <jsp:param name="page" value="notifications" />
</jsp:include>

<div class="header">
    <h1>Mes notifications</h1>
    <p class="subtitle">Vue d'ensemble de vos notifications récentes</p>
</div>

<div class="card" style="max-width: 900px;">
    <div class="card-header">
        <i class="fas fa-bell" style="margin-right: 12px;"></i>Mes notifications
    </div>

    <div class="notif-list">
        <c:choose>
            <c:when test="${empty notifications}">
                <div style="padding: 20px;">
                    <p style="font-size: 14px; color: #777;">
                        Vous n'avez aucune notification.
                    </p>
                </div>
            </c:when>

            <c:otherwise>
                <c:forEach var="nu" items="${notifications}" varStatus="status">
                    <div class="notif-row ${nu.estLu ? 'lu' : ''}">
                        <!-- Partie texte -->
                        <div class="notif-main">
                            <div class="notif-type">
                                ${nu.notification.type}
                            </div>

                            <div class="notif-message">
                                ${nu.notification.message}
                            </div>

                            <div class="notif-meta">
                                Crée le :  ${nu.dateCreationFormatee}
                                <c:choose>
                                    <c:when test="${nu.estLu}">
                                        &nbsp;• Lu le : ${nu.dateCreationFormatee}
                                    </c:when>
                                    <c:otherwise>
                                        &nbsp;• <span style="color:#C19A54;">Non lue</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- Actions -->
                        <div class="notif-actions">
                            <c:if test="${!nu.estLu}">
                                <form method="post" action="${pageContext.request.contextPath}/app/notifications">
                                    <input type="hidden" name="action" value="marquerLu">
                                    <input type="hidden" name="notificationUserId" value="${nu.id}">
                                    <button type="submit" class="btn btn-secondary">
                                        Marquer comme lue
                                    </button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<style>
    .notif-list {
        padding: 24px;
    }

    .notif-row {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        padding: 16px 0;
        border-bottom: 1px solid #F5F5F5;
    }

    .notif-row:last-child {
        border-bottom: none;
    }

    .notif-row.lu {
        opacity: 0.6;
    }

    .notif-main {
        max-width: 650px;
    }

    .notif-type {
        font-size: 12px;
        font-weight: 600;
        letter-spacing: 1px;
        text-transform: uppercase;
        color: #999999;
        margin-bottom: 6px;
    }

    .notif-message {
        font-size: 14px;
        color: #444444;
        margin-bottom: 6px;
    }

    .notif-meta {
        font-size: 12px;
        color: #999999;
    }

    .notif-actions .btn {
        font-size: 13px;
        white-space: nowrap;
    }
</style>

<jsp:include page="layout/footer.jsp" />
