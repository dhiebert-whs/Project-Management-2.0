# FRC Project Management System - Deployment Guide

## Overview

This comprehensive deployment guide covers the production deployment of the FRC Project Management System using modern containerized deployment with Spring Boot, nginx reverse proxy, SSL termination, and automated operational features.

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   nginx Proxy   │    │   Spring Boot   │    │   SQLite DB     │
│   (Port 80/443) │────│   Application   │────│   + File Store  │
│                 │    │   (Port 8080)   │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                │
                       ┌─────────────────┐
                       │   Backup System │
                       │   (Daily 2 AM)  │
                       └─────────────────┘
```

## Prerequisites

### System Requirements
- **Docker 20.10+** - Container runtime
- **Docker Compose 2.0+** - Container orchestration
- **Java 21** - Application runtime
- **Maven 3.11+** - Build system
- **curl** - Health checks and API testing
- **openssl** - SSL certificate generation

### Server Requirements
- **Minimum**: 2 GB RAM, 2 CPU cores, 20 GB storage
- **Recommended**: 4 GB RAM, 4 CPU cores, 50 GB storage
- **Operating System**: Linux (Ubuntu 20.04+, CentOS 8+, RHEL 8+)

## External Account Requirements

### Required External Services

#### 1. Google OAuth2 (For Mentor Authentication)
**Purpose**: Secure mentor login with Google accounts

**Setup Steps**:
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create new project or select existing project
3. Navigate to "APIs & Services" > "Credentials"
4. Click "Create Credentials" > "OAuth 2.0 Client IDs"
5. Configure OAuth consent screen with application details
6. Set application type to "Web application"
7. Add authorized redirect URIs:
   - `http://localhost:8080/login/oauth2/code/google` (development)
   - `https://yourdomain.com/login/oauth2/code/google` (production)
8. Note down `Client ID` and `Client Secret`

**Configuration**:
```bash
# Add to environment variables
GOOGLE_CLIENT_ID=your_google_client_id_here
GOOGLE_CLIENT_SECRET=your_google_client_secret_here
```

#### 2. Email Service (For COPPA Compliance)
**Purpose**: Send parental consent emails and system notifications

**Options**:

**Option A: SendGrid (Recommended)**
1. Sign up at [SendGrid](https://sendgrid.com/)
2. Create API key with "Mail Send" permissions
3. Verify sender domain/email
4. Configure in application:
```bash
SENDGRID_API_KEY=your_sendgrid_api_key
EMAIL_FROM=noreply@yourdomain.com
```

**Option B: Amazon SES**
1. Set up AWS account and enable SES
2. Verify domain and configure DNS records
3. Create IAM user with SES send permissions
4. Configure credentials:
```bash
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_REGION=us-east-1
EMAIL_FROM=noreply@yourdomain.com
```

**Option C: Gmail SMTP (Development Only)**
1. Enable 2-factor authentication on Gmail account
2. Generate app-specific password
3. Configure SMTP settings:
```bash
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your_app_password
```

### Optional External Services

#### 3. Cloud Storage (For File Uploads)
**Purpose**: Store project files, images, and attachments

**Amazon S3**:
```bash
AWS_S3_BUCKET=your-bucket-name
AWS_S3_REGION=us-east-1
# Use same AWS credentials as SES
```

**Google Cloud Storage**:
```bash
GOOGLE_CLOUD_BUCKET=your-bucket-name
GOOGLE_CLOUD_PROJECT_ID=your-project-id
# Use service account key file
```

#### 4. Monitoring & Analytics (Production Recommended)

**Sentry (Error Tracking)**:
1. Sign up at [Sentry.io](https://sentry.io/)
2. Create new project for Java/Spring Boot
3. Get DSN from project settings
4. Configure:
```bash
SENTRY_DSN=https://your-dsn@sentry.io/project-id
```

**Google Analytics (Usage Tracking)**:
```bash
GOOGLE_ANALYTICS_ID=GA-XXXXXXXXX-X
```

## Quick Start Deployment

### 1. Clone and Navigate
```bash
git clone <repository-url>
cd "Project Management 2.0"
```

### 2. Configure Environment
```bash
# Create production environment file
cp .env.example .env

# Edit configuration with your values
nano .env
```

### 3. Deploy Application
```bash
# Make deploy script executable
chmod +x deploy.sh

# Run full deployment
./deploy.sh deploy
```

### 4. Access Application
- **HTTP**: http://localhost (redirects to HTTPS)
- **HTTPS**: https://localhost
- **Health Check**: http://localhost:8080/actuator/health

## Detailed Configuration

### Environment Variables

Create comprehensive `.env` file in project root:

```bash
# ================================
# SPRING BOOT APPLICATION
# ================================
SPRING_PROFILES_ACTIVE=production
JAVA_OPTS=-Xmx1024m -Xms512m -XX:+UseG1GC

# ================================
# DATABASE CONFIGURATION
# ================================
DB_PATH=/app/data/frc-project.db
DB_BACKUP_PATH=/app/backups

# ================================
# SECURITY CONFIGURATION
# ================================
# ⚠️ CRITICAL: Change these immediately after first deployment
ADMIN_USERNAME=admin
ADMIN_PASSWORD=CHANGE_THIS_IMMEDIATELY

# Session management
SESSION_TIMEOUT=30m
REMEMBER_ME_KEY=your-unique-remember-me-key

# ================================
# GOOGLE OAUTH2 (REQUIRED)
# ================================
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
OAUTH_REDIRECT_URI=https://yourdomain.com/login/oauth2/code/google

# ================================
# EMAIL CONFIGURATION (REQUIRED FOR COPPA)
# ================================
# Option 1: SendGrid
SENDGRID_API_KEY=your_sendgrid_api_key
EMAIL_FROM=noreply@yourdomain.com

# Option 2: SMTP
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your_app_password

# ================================
# SSL CONFIGURATION
# ================================
SSL_CERT_PATH=/etc/nginx/ssl/cert.pem
SSL_KEY_PATH=/etc/nginx/ssl/key.pem

# ================================
# BACKUP CONFIGURATION
# ================================
BACKUP_SCHEDULE=0 2 * * *  # Daily at 2 AM
BACKUP_RETENTION_DAYS=7
BACKUP_S3_BUCKET=your-backup-bucket (optional)

# ================================
# QR CODE SERVICE
# ================================
QR_CODE_BASE_URL=https://yourdomain.com
QR_CODE_EXPIRY_HOURS=24

# ================================
# MONITORING & ANALYTICS (OPTIONAL)
# ================================
SENTRY_DSN=https://your-dsn@sentry.io/project-id
GOOGLE_ANALYTICS_ID=GA-XXXXXXXXX-X

# ================================
# CLOUD STORAGE (OPTIONAL)
# ================================
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_S3_BUCKET=your-bucket-name
AWS_REGION=us-east-1
```

### Production Application Properties

The application automatically uses `application-production.yml` in production:

```yaml
spring:
  profiles:
    active: production
  
  # Database Configuration
  datasource:
    url: jdbc:sqlite:${DB_PATH:/app/data/frc-project.db}
    driver-class-name: org.sqlite.JDBC
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      idle-timeout: 300000
      connection-timeout: 20000
  
  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        use_sql_comments: false
        generate_statistics: false
  
  # Web Configuration
  web:
    resources:
      cache:
        cachecontrol:
          max-age: 365d
          cache-public: true
  
  # Security Configuration
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - openid
              - profile
              - email

# Server Configuration
server:
  port: 8080
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,application/json,application/javascript
  error:
    include-stacktrace: never
    include-message: never

# Logging Configuration
logging:
  level:
    org.frcpm: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
  file:
    name: /app/logs/application.log
  logback:
    rollingpolicy:
      file-name-pattern: /app/logs/application-%d{yyyy-MM-dd}.%i.gz
      max-file-size: 100MB
      max-history: 30

# Management Endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when_authorized
  health:
    db:
      enabled: true
```

## SSL Configuration

### Self-Signed Certificates (Development/Testing)
```bash
# Generate self-signed certificates
mkdir -p ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout ./ssl/key.pem \
    -out ./ssl/cert.pem \
    -subj "/C=US/ST=State/L=City/O=FRC Team/CN=localhost"
```

### Production SSL Certificates

#### Option 1: Let's Encrypt (Free - Recommended)
```bash
# Install Certbot
sudo apt-get update
sudo apt-get install certbot python3-certbot-nginx

# Generate certificate
sudo certbot --nginx -d yourdomain.com

# Auto-renewal (add to crontab)
0 12 * * * /usr/bin/certbot renew --quiet
```

#### Option 2: Commercial SSL Certificate
```bash
# Generate Certificate Signing Request
openssl req -new -newkey rsa:2048 -nodes \
    -keyout yourdomain.com.key \
    -out yourdomain.com.csr

# Submit CSR to certificate authority
# Download certificate files and install:
cp yourdomain.com.crt ./ssl/cert.pem
cp yourdomain.com.key ./ssl/key.pem
cp intermediate.crt ./ssl/intermediate.pem
```

## Deployment Commands

### Full Deployment Pipeline
```bash
# Complete deployment with all steps
./deploy.sh deploy
```

This runs the following sequence:
1. **Tests** - Run complete test suite
2. **Build** - Maven clean package with production profile
3. **Docker Build** - Create application container
4. **Backup** - Create pre-deployment database backup
5. **Deploy** - Start/restart containers with zero downtime
6. **Health Check** - Verify application startup

### Individual Operations
```bash
# Stop all services
./deploy.sh stop

# View application logs
./deploy.sh logs

# Create manual backup
./deploy.sh backup

# Check application health
./deploy.sh health

# Clean up Docker resources
./deploy.sh clean

# Update SSL certificates
./deploy.sh ssl-renew
```

## Security Considerations

### Production Security Checklist

**Before Going Live**:
- [ ] Change default admin password during first login
- [ ] Replace self-signed certificates with proper SSL
- [ ] Configure proper domain name and DNS
- [ ] Set up firewall rules (ports 80, 443, 22 only)
- [ ] Configure fail2ban for SSH protection
- [ ] Enable automatic security updates
- [ ] Set up monitoring and alerting
- [ ] Test backup and restore procedures
- [ ] Document all credentials securely
- [ ] Train team members on maintenance procedures

**Critical Security Notes**:
- **⚠️ Admin Password**: Default credentials (admin/secure_password) MUST be changed immediately
- **🔒 HTTPS Only**: Production must enforce HTTPS redirect
- **🔐 OAuth2**: Google OAuth2 requires proper domain verification
- **👶 COPPA**: Email service required for parental consent workflows
- **📧 Email Security**: Use authenticated SMTP with TLS encryption

### Network Security
```nginx
# Rate limiting configuration
limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
limit_req_zone $binary_remote_addr zone=login:10m rate=5r/m;

# Security headers
add_header X-Frame-Options DENY;
add_header X-Content-Type-Options nosniff;
add_header X-XSS-Protection "1; mode=block";
add_header Strict-Transport-Security "max-age=63072000; includeSubdomains; preload";
```

## Data Management

### Directory Structure
```
./
├── data/                 # SQLite database and uploads
│   ├── frc-project.db   # Main database file
│   ├── uploads/         # File attachments
│   └── qr-codes/        # Generated QR codes
├── logs/                 # Application logs
│   ├── application.log  # Current log file
│   └── archived/        # Rotated logs
├── backups/              # Database backups
│   ├── daily/           # Daily automated backups
│   └── manual/          # Manual backups
├── ssl/                  # SSL certificates
│   ├── cert.pem        # SSL certificate
│   └── key.pem         # Private key
└── docker-compose.yml    # Container configuration
```

### Backup Strategy

**Automated Daily Backups**:
- Database backup at 2 AM daily
- 7-day local retention
- Optional cloud backup to S3/GCS
- Automatic cleanup of old backups

**Manual Backup Commands**:
```bash
# Create immediate backup
./deploy.sh backup

# Restore from specific backup
./deploy.sh restore backup_20241201_020000.tar.gz

# List available backups
./deploy.sh list-backups
```

**Backup Contents**:
- SQLite database file
- User uploaded files
- Generated QR codes
- Application configuration
- SSL certificates

### Database Maintenance

**Routine Operations**:
```bash
# Database optimization (monthly)
docker exec frc-app sqlite3 /app/data/frc-project.db "VACUUM; ANALYZE;"

# Check database integrity
docker exec frc-app sqlite3 /app/data/frc-project.db "PRAGMA integrity_check;"

# Database statistics
docker exec frc-app sqlite3 /app/data/frc-project.db ".schema" | wc -l
```

## Monitoring & Health Checks

### Application Health Endpoints

**Health Check URLs**:
- **Basic Health**: `GET /actuator/health`
- **Detailed Health**: `GET /actuator/health` (with admin auth)
- **Application Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`

**Health Check Script**:
```bash
#!/bin/bash
# health-check.sh
HEALTH_URL="http://localhost:8080/actuator/health"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_URL)

if [ $RESPONSE -eq 200 ]; then
    echo "✅ Application is healthy"
    exit 0
else
    echo "❌ Application health check failed: HTTP $RESPONSE"
    exit 1
fi
```

### Log Monitoring

**Application Logs**:
```bash
# Real-time log monitoring
tail -f logs/application.log

# Error log filtering
grep -i "error\|exception" logs/application.log

# Performance monitoring
grep "slow query" logs/application.log
```

**nginx Access Logs**:
```bash
# Monitor access patterns
tail -f /var/log/nginx/access.log

# Top requesting IPs
awk '{print $1}' /var/log/nginx/access.log | sort | uniq -c | sort -nr
```

### Performance Monitoring

**Key Metrics to Monitor**:
- Response time for critical endpoints
- Database query performance
- Memory usage and garbage collection
- WebSocket connection count
- Active user sessions

**Monitoring Setup**:
```bash
# Add to crontab for automated monitoring
*/5 * * * * /path/to/health-check.sh >> /var/log/frc-health.log 2>&1
```

## Troubleshooting

### Common Issues

**Port Conflicts**:
```bash
# Check what's using port 8080
sudo lsof -i :8080
sudo netstat -tulpn | grep 8080

# Find and stop conflicting service
sudo systemctl stop conflicting-service
```

**SSL Certificate Issues**:
```bash
# Test SSL certificate
openssl x509 -in ssl/cert.pem -text -noout

# Verify certificate chain
openssl verify -CAfile ca-bundle.crt ssl/cert.pem

# Regenerate self-signed certificates
rm -rf ssl/
./deploy.sh deploy  # Will regenerate certificates
```

**Database Issues**:
```bash
# Check database file permissions
ls -la data/frc-project.db

# Backup and restore database
./deploy.sh stop
cp data/frc-project.db data/frc-project.db.backup
sqlite3 data/frc-project.db ".recover" | sqlite3 data/frc-project-recovered.db
mv data/frc-project-recovered.db data/frc-project.db
./deploy.sh deploy
```

**Memory Issues**:
```bash
# Increase JVM memory in .env file
JAVA_OPTS=-Xmx2048m -Xms1024m -XX:+UseG1GC

# Monitor memory usage
docker stats frc-app
```

### Emergency Procedures

**Application Won't Start**:
1. Check application logs: `./deploy.sh logs`
2. Verify database permissions and integrity
3. Check available disk space: `df -h`
4. Verify all environment variables are set
5. Test with minimal configuration

**Data Recovery**:
1. Stop application: `./deploy.sh stop`
2. Restore from latest backup: `./deploy.sh restore`
3. Verify data integrity
4. Restart application: `./deploy.sh deploy`

**SSL Certificate Expired**:
1. Renew certificate: `./deploy.sh ssl-renew`
2. Or use emergency self-signed: `./deploy.sh ssl-emergency`
3. Restart nginx: `docker-compose restart frc-nginx`

## Maintenance Schedule

### Daily Automated Tasks
- **2:00 AM** - Database backup
- **3:00 AM** - Log rotation
- **4:00 AM** - System health report

### Weekly Manual Tasks
- Review application logs for errors
- Check disk space usage
- Verify backup integrity
- Update system packages

### Monthly Tasks
- Security patch updates
- SSL certificate renewal check
- Database optimization (`VACUUM`)
- Performance review and tuning

### Quarterly Tasks
- Full security audit
- Disaster recovery test
- User access review
- Documentation updates

## Support & Documentation

### Getting Help

**Check These First**:
1. Application logs: `./deploy.sh logs`
2. Health status: `./deploy.sh health`
3. Container status: `docker-compose ps`
4. System resources: `htop` or `docker stats`

**Log Locations**:
- **Application**: `./logs/application.log`
- **nginx**: `/var/log/nginx/`
- **Container**: `docker-compose logs <service-name>`
- **System**: `/var/log/syslog`

### Contact Information

**For Production Issues**:
- Include application logs (last 100 lines)
- System specifications and resource usage
- Error reproduction steps
- Current configuration (sanitized)

### Deployment Checklist

**Pre-Deployment**:
- [ ] All environment variables configured
- [ ] SSL certificates obtained and installed
- [ ] Email service configured and tested
- [ ] Google OAuth2 setup and verified
- [ ] Firewall rules configured
- [ ] DNS records pointed to server
- [ ] Backup strategy implemented

**Post-Deployment**:
- [ ] Health checks passing
- [ ] Admin password changed from default
- [ ] User registration working (including COPPA)
- [ ] Email notifications sending
- [ ] WebSocket real-time features working
- [ ] SSL certificate valid and auto-renewing
- [ ] Monitoring and alerting configured
- [ ] Team training completed

## License

This deployment configuration is part of the FRC Project Management System developed for educational robotics communities.