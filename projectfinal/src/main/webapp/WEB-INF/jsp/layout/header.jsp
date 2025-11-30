<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${param.title != null ? param.title : 'Système de Gestion RH'}</title>
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
            background: #FAFAFA;
            color: #1A1A1A;
            font-size: 14px;
            line-height: 1.6;
        }

        .container {
            display: flex;
            min-height: 100vh;
        }

        .sidebar {
            width: 280px;
            background: #1A1A1A;
            color: #FFFFFF;
            padding: 40px 0;
            position: fixed;
            height: 100vh;
            overflow-y: auto;
            z-index: 1000;
        }

        .logo {
            font-family: 'Playfair Display', serif;
            font-size: 28px;
            font-weight: 700;
            color: #C5A572;
            margin-bottom: 48px;
            padding: 0 32px;
            letter-spacing: 1px;
        }

        .user-info {
            background: rgba(197, 165, 114, 0.1);
            border-top: 1px solid rgba(197, 165, 114, 0.2);
            border-bottom: 1px solid rgba(197, 165, 114, 0.2);
            padding: 24px 32px;
            margin-bottom: 32px;
            text-align: center;
        }

        .user-info i {
            font-size: 40px;
            color: #C5A572;
            margin-bottom: 12px;
            display: block;
        }

        .user-info h6 {
            font-size: 16px;
            font-weight: 600;
            color: #FFFFFF;
            margin-bottom: 4px;
            letter-spacing: 0.5px;
        }

        .user-info small {
            font-size: 12px;
            color: #C5A572;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .nav-link {
            display: flex;
            align-items: center;
            padding: 16px 32px;
            color: #B0B0B0;
            text-decoration: none;
            transition: all 0.3s ease;
            font-size: 14px;
            font-weight: 400;
            border-left: 3px solid transparent;
        }

        .nav-link i {
            width: 24px;
            margin-right: 16px;
            font-size: 16px;
        }

        .nav-link:hover {
            color: #FFFFFF;
            background: rgba(255, 255, 255, 0.05);
        }

        .nav-link.active {
            color: #C5A572;
            background: rgba(197, 165, 114, 0.1);
            border-left-color: #C5A572;
            font-weight: 500;
        }

        .main-content {
            margin-left: 280px;
            flex: 1;
            padding: 48px;
            min-height: 100vh;
        }

        .header {
            margin-bottom: 48px;
            border-bottom: 1px solid #E0E0E0;
            padding-bottom: 24px;
        }

        .header h1 {
            font-family: 'Playfair Display', serif;
            font-size: 42px;
            font-weight: 700;
            color: #1A1A1A;
            margin-bottom: 8px;
            letter-spacing: -1px;
        }

        .header .subtitle {
            font-size: 16px;
            color: #666666;
            font-weight: 300;
        }

        .card {
            background: #FFFFFF;
            border: 1px solid #E0E0E0;
            padding: 32px;
            margin-bottom: 24px;
            transition: all 0.3s ease;
        }

        .card:hover {
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
        }

        .card-header {
            font-family: 'Playfair Display', serif;
            font-size: 24px;
            font-weight: 600;
            color: #1A1A1A;
            margin-bottom: 24px;
            padding-bottom: 16px;
            border-bottom: 1px solid #E0E0E0;
        }

        .btn {
            display: inline-block;
            padding: 14px 28px;
            font-size: 13px;
            font-weight: 500;
            text-decoration: none;
            transition: all 0.3s ease;
            border: 1px solid #1A1A1A;
            cursor: pointer;
            text-align: center;
            text-transform: uppercase;
            letter-spacing: 1px;
            font-family: 'Inter', sans-serif;
        }

        .btn-primary {
            background: #1A1A1A;
            color: #C5A572;
            border-color: #1A1A1A;
        }

        .btn-primary:hover {
            background: #C5A572;
            color: #1A1A1A;
            border-color: #C5A572;
        }

        .btn-secondary {
            background: transparent;
            color: #1A1A1A;
            border: 1px solid #E0E0E0;
        }

        .btn-secondary:hover {
            background: #1A1A1A;
            color: #FFFFFF;
            border-color: #1A1A1A;
        }

        .btn-danger {
            background: #DC3545;
            color: #FFFFFF;
            border: 1px solid #DC3545;
        }

        .btn-danger:hover {
            background: #C82333;
            border-color: #C82333;
        }

        .btn-danger:disabled {
            background: #E0E0E0;
            color: #999999;
            border-color: #E0E0E0;
            cursor: not-allowed;
            opacity: 0.6;
        }

        .btn-outline {
            background: transparent;
            color: #C5A572;
            border: 1px solid #C5A572;
        }

        .btn-outline:hover {
            background: #C5A572;
            color: #1A1A1A;
        }

        .table-elegant {
            width: 100%;
            border-collapse: separate;
            border-spacing: 0;
        }

        .table-elegant thead th {
            background: #FAFAFA;
            color: #1A1A1A;
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 1.5px;
            font-weight: 600;
            padding: 16px;
            border-bottom: 2px solid #E0E0E0;
            text-align: left;
        }

        .table-elegant tbody td {
            padding: 20px 16px;
            border-bottom: 1px solid #F5F5F5;
            color: #1A1A1A;
            font-size: 14px;
        }

        .table-elegant tbody tr {
            transition: background 0.2s ease;
        }

        .table-elegant tbody tr:hover {
            background: #FAFAFA;
        }

        .badge-elegant {
            display: inline-block;
            padding: 6px 14px;
            font-size: 11px;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 1px;
            border: 1px solid;
        }

        .badge-success {
            background: rgba(197, 165, 114, 0.1);
            color: #C5A572;
            border-color: #C5A572;
        }

        .badge-warning {
            background: rgba(0, 0, 0, 0.05);
            color: #666666;
            border-color: #E0E0E0;
        }

        .badge-danger {
            background: rgba(0, 0, 0, 0.1);
            color: #1A1A1A;
            border-color: #1A1A1A;
        }

        .badge-info {
            background: transparent;
            color: #999999;
            border-color: #E0E0E0;
        }

        .form-group {
            margin-bottom: 24px;
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

        .form-control {
            width: 100%;
            padding: 14px 16px;
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

        .form-control::placeholder {
            color: #999999;
            font-weight: 300;
        }

        textarea.form-control {
            resize: vertical;
            min-height: 120px;
        }

        .alert {
            padding: 20px 24px;
            margin-bottom: 24px;
            border-left: 3px solid;
            background: #FAFAFA;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        .alert-success {
            border-left-color: #22C55E;
            background: #F0FDF4;
            color: #166534;
            border-left-width: 5px;
        }
        
        @keyframes slideDown {
            from {
                opacity: 0;
                transform: translateY(-20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .alert-danger {
            border-left-color: #1A1A1A;
            color: #1A1A1A;
        }

        .alert-warning {
            border-left-color: #666666;
            color: #1A1A1A;
        }

        .alert-info {
            border-left-color: #E0E0E0;
            color: #666666;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 24px;
            margin-bottom: 48px;
        }

        .stat-card {
            background: #FFFFFF;
            border: 1px solid #E0E0E0;
            padding: 32px 24px;
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
        }

        .stat-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 2px;
            background: #C5A572;
            transform: scaleX(0);
            transition: transform 0.3s ease;
        }

        .stat-card:hover::before {
            transform: scaleX(1);
        }

        .stat-card:hover {
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
            transform: translateY(-4px);
        }

        .stat-card i {
            font-size: 24px;
            color: #C5A572;
            margin-bottom: 16px;
        }

        .stat-card h4 {
            font-size: 36px;
            font-weight: 300;
            color: #1A1A1A;
            margin-bottom: 8px;
        }

        .stat-card p {
            font-size: 11px;
            color: #666666;
            text-transform: uppercase;
            letter-spacing: 1.5px;
            font-weight: 500;
        }

        .avatar {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            background: #C5A572;
            color: #1A1A1A;
            font-weight: 600;
            font-size: 16px;
            text-transform: uppercase;
        }

        .avatar-lg {
            width: 80px;
            height: 80px;
            font-size: 28px;
        }

        .divider {
            height: 1px;
            background: #E0E0E0;
            margin: 32px 0;
        }

        .text-muted {
            color: #999999 !important;
        }

        .text-accent {
            color: #C5A572 !important;
        }

        .grid-2 {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 24px;
        }

        .grid-3 {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 24px;
        }

        @media (max-width: 1024px) {
            .sidebar {
                width: 240px;
            }
            .main-content {
                margin-left: 240px;
            }
            .stats-grid {
                grid-template-columns: repeat(2, 1fr);
            }
        }

        @media (max-width: 768px) {
            .sidebar {
                transform: translateX(-100%);
            }
            .main-content {
                margin-left: 0;
                padding: 24px;
            }
            .stats-grid {
                grid-template-columns: 1fr;
            }
            .grid-2, .grid-3 {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">
    <div class="container">
        <!-- Sidebar -->
        <aside class="sidebar">
            <div class="logo">RH ÉLÉGANCE</div>
            
            <a href="${pageContext.request.contextPath}/app/notifications"
			   class="notif-icon"${param.page == 'notification' ? 'active' : ''}
			   style="
			       position: relative;
			       padding: 0 32px;
			       margin-bottom: 24px;
			       cursor: pointer;
			       display: flex;
			       align-items: center;
			       gap: 16px;
			       text-decoration: none;
			       color: inherit;
			   ">
			    <i class="fa fa-bell" style="font-size: 22px; color: #C5A572;"></i>
			
			    <span style="color:#C5A572; font-size:14px;">Notifications</span>
			
			    <span id="notif-badge" style="
			        background:#C92A2A;
			        color:white;
			        padding:2px 7px;
			        font-size:11px;
			        border-radius:50%;
			        position:absolute;
			        right:18px;
			        top:-4px;
			        display:none;
			    ">0</span>
			</a>



            <div class="user-info">
                <i class="fas fa-user-circle"></i>
                <a href="${pageContext.request.contextPath}/app/profil" style="text-decoration: none; color: inherit;">
                    <h6 style="cursor: pointer; transition: color 0.3s;" onmouseover="this.style.color='#C5A572'" onmouseout="this.style.color='#FFFFFF'">
                        ${utilisateur.username != null ? utilisateur.username : 'Utilisateur'}
                    </h6>
                </a>
                <small>${utilisateur.role != null ? utilisateur.role.libelle : 'Invité'}</small>
            </div>

            <nav>
                <a href="${pageContext.request.contextPath}/app/dashboard" 
                   class="nav-link ${param.page == 'dashboard' ? 'active' : ''}">
                    <i class="fas fa-chart-line"></i>
                    <span>Tableau de bord</span>
                </a>

               	<a href="${pageContext.request.contextPath}/app/employes" 
                    class="nav-link ${param.page == 'employes' ? 'active' : ''}">
                    <i class="fas fa-users"></i>
               		<span>Employés</span>
              	</a>
           

               
   	           	<a href="${pageContext.request.contextPath}/app/departements" 
                    class="nav-link ${param.page == 'departements' ? 'active' : ''}">
                    <i class="fas fa-building"></i>
   		            <span>Départements</span>
                </a>
                

                <a href="${pageContext.request.contextPath}/app/projets" 
                   class="nav-link ${param.page == 'projets' ? 'active' : ''}">
                    <i class="fas fa-project-diagram"></i>
                    <span>Projets</span>
                </a>

                <a href="${pageContext.request.contextPath}/app/fiches-paie" 
                	class="nav-link ${param.page == 'fiches-paie' ? 'active' : ''}">
                    <i class="fas fa-file-invoice-dollar"></i>
                    <span>Fiches de paie</span>
               	</a>

                <a href="${pageContext.request.contextPath}/app/conges-absences" 
                   class="nav-link ${param.page == 'conges' ? 'active' : ''}">
                    <i class="fas fa-calendar-check"></i>
                    <span>Congés</span>
                </a>

                <c:if test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH'}">
                    <a href="${pageContext.request.contextPath}/app/statistiques" 
                       class="nav-link ${param.page == 'statistiques' ? 'active' : ''}">
                        <i class="fas fa-chart-bar"></i>
                        <span>Statistiques</span>
                    </a>
                </c:if>

                <div style="height: 1px; background: rgba(255,255,255,0.1); margin: 24px 0;"></div>

                <a href="${pageContext.request.contextPath}/logout" class="nav-link">
                    <i class="fas fa-sign-out-alt"></i>
                    <span>Déconnexion</span>
                </a>
            </nav>
        </aside>

        <!-- Main Content -->
        <main class="main-content">
