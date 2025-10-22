<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Erreur - Système RH</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@600;700&family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Inter', sans-serif;
            background: #FAFAFA;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            padding: 24px;
        }
        
        .error-container {
            max-width: 600px;
            width: 100%;
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
            padding: 64px 48px;
            text-align: center;
        }
        
        .error-icon {
            width: 120px;
            height: 120px;
            margin: 0 auto 32px;
            background: linear-gradient(135deg, #C5A572 0%, #A08555 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 56px;
            color: white;
        }
        
        h1 {
            font-family: 'Playfair Display', serif;
            font-size: 42px;
            font-weight: 700;
            color: #1A1A1A;
            margin-bottom: 16px;
        }
        
        .error-code {
            font-family: 'Courier New', monospace;
            font-size: 18px;
            font-weight: 600;
            color: #C5A572;
            margin-bottom: 24px;
            letter-spacing: 2px;
        }
        
        .error-message {
            font-size: 16px;
            color: #666666;
            line-height: 1.6;
            margin-bottom: 32px;
        }
        
        .btn {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 14px 28px;
            background: #1A1A1A;
            color: white;
            text-decoration: none;
            border-radius: 8px;
            font-weight: 600;
            font-size: 14px;
            letter-spacing: 0.5px;
            transition: all 0.3s ease;
            border: 2px solid #1A1A1A;
        }
        
        .btn:hover {
            background: white;
            color: #1A1A1A;
        }
        
        .btn-secondary {
            background: white;
            color: #1A1A1A;
            margin-left: 12px;
        }
        
        .btn-secondary:hover {
            background: #1A1A1A;
            color: white;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-icon">
            <i class="fas fa-exclamation-triangle"></i>
        </div>
        
        <h1>Oups !</h1>
        
        <div class="error-code">
            ERREUR ${pageContext.errorData != null ? pageContext.errorData.statusCode : '500'}
        </div>
        
        <div class="error-message">
            <c:choose>
                <c:when test="${not empty error}">
                    ${error}
                </c:when>
                <c:when test="${pageContext.errorData != null && pageContext.errorData.statusCode == 404}">
                    La page que vous recherchez est introuvable.
                </c:when>
                <c:when test="${pageContext.errorData != null && pageContext.errorData.statusCode == 403}">
                    Vous n'avez pas l'autorisation d'accéder à cette ressource.
                </c:when>
                <c:when test="${pageContext.errorData != null && pageContext.errorData.statusCode == 500}">
                    Une erreur interne du serveur s'est produite. Veuillez réessayer plus tard.
                </c:when>
                <c:otherwise>
                    Une erreur inattendue s'est produite. Notre équipe a été notifiée.
                </c:otherwise>
            </c:choose>
        </div>
        
        <c:if test="${not empty pageContext.errorData.throwable}">
            <div style="margin-bottom: 32px; padding: 16px; background: #F5F5F5; border-radius: 8px; text-align: left;">
                <div style="font-family: 'Courier New', monospace; font-size: 12px; color: #666666; word-break: break-word;">
                    ${pageContext.errorData.throwable.message}
                </div>
            </div>
        </c:if>
        
        <div>
            <a href="${pageContext.request.contextPath}/app/dashboard" class="btn">
                <i class="fas fa-home"></i>
                Retour à l'accueil
            </a>
            <a href="javascript:history.back()" class="btn btn-secondary">
                <i class="fas fa-arrow-left"></i>
                Page précédente
            </a>
        </div>
    </div>
</body>
</html>
