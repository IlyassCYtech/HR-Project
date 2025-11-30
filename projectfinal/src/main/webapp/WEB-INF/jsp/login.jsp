<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connexion - RH Élégance</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@600;700&family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Inter', sans-serif;
            background: #1A1A1A;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 24px;
        }

        .login-container {
            max-width: 480px;
            width: 100%;
        }

        .login-card {
            background: #FFFFFF;
            border: 1px solid #E0E0E0;
            padding: 48px;
        }

        .logo {
            font-family: 'Playfair Display', serif;
            font-size: 32px;
            font-weight: 700;
            color: #C5A572;
            text-align: center;
            margin-bottom: 12px;
            letter-spacing: 1px;
        }

        .login-subtitle {
            text-align: center;
            font-size: 14px;
            color: #666666;
            margin-bottom: 48px;
            text-transform: uppercase;
            letter-spacing: 2px;
            font-weight: 300;
        }

        .alert-elegant {
            padding: 16px 20px;
            margin-bottom: 32px;
            border-left: 3px solid #1A1A1A;
            background: #FAFAFA;
            color: #1A1A1A;
            font-size: 13px;
        }

        .alert-elegant i {
            margin-right: 8px;
            color: #C5A572;
        }

        .form-group {
            margin-bottom: 28px;
        }

        .form-label {
            display: block;
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 1.5px;
            font-weight: 600;
            color: #1A1A1A;
            margin-bottom: 8px;
        }

        .form-label i {
            margin-right: 8px;
            color: #C5A572;
        }

        .form-control {
            width: 100%;
            padding: 16px;
            font-size: 14px;
            font-family: 'Inter', sans-serif;
            border: 1px solid #E0E0E0;
            background: #FFFFFF;
            color: #1A1A1A;
            transition: all 0.3s ease;
        }

        .form-control:focus {
            outline: none;
            border-color: #C5A572;
            box-shadow: 0 0 0 3px rgba(197, 165, 114, 0.1);
        }

        .btn-login {
            width: 100%;
            padding: 16px 28px;
            font-size: 13px;
            font-weight: 500;
            text-decoration: none;
            transition: all 0.3s ease;
            border: 1px solid #1A1A1A;
            cursor: pointer;
            text-align: center;
            text-transform: uppercase;
            letter-spacing: 1.5px;
            font-family: 'Inter', sans-serif;
            background: #1A1A1A;
            color: #C5A572;
            margin-top: 8px;
        }

        .btn-login:hover {
            background: #C5A572;
            color: #1A1A1A;
            border-color: #C5A572;
            transform: translateY(-2px);
        }

        .divider {
            height: 1px;
            background: #E0E0E0;
            margin: 40px 0;
        }

        .demo-section {
            background: #FAFAFA;
            padding: 24px;
            border: 1px solid #F0F0F0;
        }

        .demo-title {
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 1.5px;
            font-weight: 600;
            color: #1A1A1A;
            margin-bottom: 16px;
        }

        .demo-title i {
            margin-right: 8px;
            color: #C5A572;
        }

        .demo-account {
            padding: 12px 0;
            border-bottom: 1px solid #E0E0E0;
            cursor: pointer;
            transition: all 0.2s ease;
            font-size: 13px;
        }

        .demo-account:last-child {
            border-bottom: none;
        }

        .demo-account:hover {
            padding-left: 8px;
            color: #C5A572;
        }

        .demo-account strong {
            display: block;
            font-weight: 600;
            color: #1A1A1A;
            margin-bottom: 4px;
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .demo-account span {
            color: #666666;
            font-weight: 300;
        }

        @media (max-width: 600px) {
            .login-card {
                padding: 32px 24px;
            }
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-card">
            <div class="logo">RH ÉLÉGANCE</div>
            <div class="login-subtitle">Système de Gestion</div>
            
            <%-- Fallback to scriptlet to avoid JSTL dependency during startup --%>
            <% if (request.getAttribute("errorMessage") != null) { %>
                <div class="alert-elegant">
                    <i class="fas fa-exclamation-triangle"></i>
                    <%= request.getAttribute("errorMessage") %>
                </div>
            <% } %>
            
            <form method="post" action="${pageContext.request.contextPath}/login">
                <div class="form-group">
                    <label for="username" class="form-label">
                        <i class="fas fa-user"></i>Nom d'utilisateur
                    </label>
                    <input type="text" 
                           class="form-control" 
                           id="username" 
                           name="username" 
                           value="${param.username}"
                           required 
                           autofocus
                           placeholder="Identifiant">
                </div>
                
                <div class="form-group">
                    <label for="password" class="form-label">
                        <i class="fas fa-lock"></i>Mot de passe
                    </label>
                    <input type="password" 
                           class="form-control" 
                           id="password" 
                           name="password" 
                           required
                           placeholder="••••••••">
                </div>
                
                <button type="submit" class="btn-login">
                    <i class="fas fa-sign-in-alt" style="margin-right: 8px;"></i>Se connecter
                </button>
            </form>
            
            <div class="divider"></div>
            
            <!-- Comptes de démonstration -->
            <div class="demo-section">
                <div class="demo-title">
                    <i class="fas fa-users"></i>Comptes de démonstration
                </div>
                
                <div class="demo-account" onclick="fillCredentials('admin', 'admin123')">
                    <strong>Administrateur</strong>
                    <span>admin / admin123</span>
                </div>
                
                <div class="demo-account" onclick="fillCredentials('marie.dubois', 'marie123')">
                    <strong>Responsable RH</strong>
                    <span>marie.dubois / marie123</span>
                </div>
                
                <div class="demo-account" onclick="fillCredentials('pierre.leroy', 'pierre123')">
                    <strong>Chef de projet</strong>
                    <span>pierre.leroy / pierre123</span>
                </div>
                
                <div class="demo-account" onclick="fillCredentials('claire.durand', 'claire123')">
                    <strong>Employé standard</strong>
                    <span>claire.durand / claire123</span>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        function fillCredentials(username, password) {
            document.getElementById('username').value = username;
            document.getElementById('password').value = password;
        }
        
        document.addEventListener('DOMContentLoaded', function() {
            const usernameField = document.getElementById('username');
            if (!usernameField.value) {
                usernameField.focus();
            }
        });
    </script>
</body>
</html>