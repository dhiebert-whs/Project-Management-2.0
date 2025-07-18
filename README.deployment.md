# FRC Project Management System - Deployment Guide

## Overview

This deployment guide covers the production deployment of the FRC Project Management System using Docker containers with nginx reverse proxy, SSL termination, and automated backups.

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

- **Docker 20.10+**
- **Docker Compose 2.0+**
- **Java 21**
- **Maven 3.9+**
- **curl** (for health checks)
- **openssl** (for SSL certificates)

## Quick Start

### 1. Clone and Navigate
```bash
git clone <repository-url>
cd "Project Management 2.0"
```

### 2. Deploy
```bash
# Make deploy script executable
chmod +x deploy.sh

# Run full deployment
./deploy.sh deploy
```

### 3. Access Application
- **HTTP**: http://localhost (redirects to HTTPS)
- **HTTPS**: https://localhost
- **Health Check**: http://localhost:8080/actuator/health

## Deployment Commands

### Full Deployment
```bash
./deploy.sh deploy
```
Runs: tests → build → docker build → backup → deploy → health check

### Other Commands
```bash
./deploy.sh stop     # Stop all containers
./deploy.sh logs     # Show application logs
./deploy.sh backup   # Create manual backup
./deploy.sh health   # Check application health
./deploy.sh clean    # Clean up Docker resources
```

## Configuration

### Environment Variables

Create `.env` file in project root:
```bash
# Application
SPRING_PROFILES_ACTIVE=production
JAVA_OPTS=-Xmx1024m -Xms512m

# Database
DB_PATH=/app/data/frc-project.db

# Security
SSL_CERT_PATH=/etc/nginx/ssl/cert.pem
SSL_KEY_PATH=/etc/nginx/ssl/key.pem

# Backup
BACKUP_SCHEDULE=0 2 * * *  # Daily at 2 AM
BACKUP_RETENTION_DAYS=7
```

### Application Properties

Production configuration in `src/main/resources/application-production.yml`:
```yaml
spring:
  profiles:
    active: production
  datasource:
    url: jdbc:sqlite:/app/data/frc-project.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

server:
  port: 8080
  compression:
    enabled: true

logging:
  level:
    org.frcpm: INFO
  file:
    name: /app/logs/application.log
```

## SSL Configuration

### Self-Signed Certificates (Development)
The deployment script automatically generates self-signed certificates:
```bash
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout ./ssl/key.pem \
    -out ./ssl/cert.pem \
    -subj "/C=US/ST=State/L=City/O=FRC Team/CN=localhost"
```

### Production Certificates
For production, replace with proper certificates:
```bash
# Copy your certificates
cp your-cert.pem ./ssl/cert.pem
cp your-key.pem ./ssl/key.pem

# Restart nginx
docker-compose restart frc-nginx
```

## Data Management

### Directory Structure
```
./
├── data/                 # SQLite database and uploads
├── logs/                 # Application logs
├── backups/              # Automated backups
├── ssl/                  # SSL certificates
└── docker-compose.yml    # Container orchestration
```

### Backup System

**Automatic Backups**:
- Daily at 2 AM
- 7-day retention
- Stored in `./backups/`

**Manual Backup**:
```bash
./deploy.sh backup
```

**Restore from Backup**:
```bash
# Stop application
./deploy.sh stop

# Extract backup
tar -xzf backups/backup_YYYYMMDD_HHMMSS.tar.gz -C data/

# Start application
./deploy.sh deploy
```

## Monitoring

### Health Checks

**Application Health**:
```bash
curl http://localhost:8080/actuator/health
```

**Container Status**:
```bash
docker-compose ps
```

**Application Logs**:
```bash
# Real-time logs
./deploy.sh logs

# Or directly
docker-compose logs -f frc-project-management
```

### Metrics Endpoints

Available at `/actuator/*`:
- `/actuator/health` - Application health
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics

## Security

### Network Security
- nginx reverse proxy with rate limiting
- SSL/TLS encryption (HTTPS only)
- Internal container network isolation

### Application Security
- Spring Security with COPPA compliance
- Role-based access control
- Session management
- CSRF protection

### Rate Limiting
- API endpoints: 10 requests/second
- Login attempts: 5 requests/minute
- Burst handling with nodelay

## Performance Tuning

### JVM Configuration
```bash
# Memory settings (adjust based on server)
JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC -XX:+UseContainerSupport"
```

### nginx Configuration
- Gzip compression enabled
- Static file caching (1 year)
- WebSocket support
- HTTP/2 enabled

### Database Optimization
- SQLite with WAL mode
- Regular VACUUM operations
- Index optimization

## Troubleshooting

### Common Issues

**Port Already in Use**:
```bash
# Check what's using port 8080
lsof -i :8080
netstat -tulpn | grep 8080

# Stop conflicting service
sudo systemctl stop conflicting-service
```

**SSL Certificate Issues**:
```bash
# Regenerate certificates
rm -rf ssl/
./deploy.sh deploy  # Will regenerate
```

**Database Corruption**:
```bash
# Stop application
./deploy.sh stop

# Restore from backup
tar -xzf backups/backup_YYYYMMDD_HHMMSS.tar.gz -C data/

# Start application
./deploy.sh deploy
```

**Memory Issues**:
```bash
# Increase memory limits in docker-compose.yml
environment:
  - JAVA_OPTS=-Xmx2048m -Xms1024m
```

### Log Locations

- **Application Logs**: `./logs/application.log`
- **nginx Logs**: `docker-compose logs frc-nginx`
- **Container Logs**: `docker-compose logs <service-name>`

## Maintenance

### Regular Tasks

**Weekly**:
- Review application logs
- Check disk space usage
- Verify backup integrity

**Monthly**:
- Update SSL certificates (if needed)
- Review security logs
- Performance analysis

**Quarterly**:
- Application updates
- Security audit
- Backup restoration test

### Updates

**Application Update**:
```bash
# Pull latest code
git pull

# Deploy new version
./deploy.sh deploy
```

**System Update**:
```bash
# Update Docker images
docker-compose pull

# Recreate containers
docker-compose up -d --force-recreate
```

## Support

### Getting Help

1. **Check Logs**: `./deploy.sh logs`
2. **Health Status**: `./deploy.sh health`
3. **Documentation**: Review this guide
4. **Community**: FRC forums and communities

### Reporting Issues

Include the following information:
- Docker and Docker Compose versions
- Application version
- Error logs
- Steps to reproduce
- System specifications

## Production Checklist

Before going live:
- [ ] Replace self-signed certificates with proper SSL
- [ ] Configure proper domain name
- [ ] Set up monitoring and alerting
- [ ] Test backup and restore procedures
- [ ] Configure firewall rules
- [ ] Set up log rotation
- [ ] Document access credentials
- [ ] Train team members on maintenance

## License

This deployment configuration is part of the FRC Project Management System.
See main project documentation for license information.