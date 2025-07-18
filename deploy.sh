#!/bin/bash

# FRC Project Management System Deployment Script
# Phase 3C: Integration & Deployment

set -e

# Configuration
APP_NAME="frc-project-management"
VERSION="2.0.0"
DOCKER_IMAGE="${APP_NAME}:${VERSION}"
BACKUP_DIR="./backups"
DATA_DIR="./data"
LOGS_DIR="./logs"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v java &> /dev/null; then
        log_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    log_success "All prerequisites are met"
}

# Create required directories
create_directories() {
    log_info "Creating required directories..."
    
    mkdir -p "${DATA_DIR}"
    mkdir -p "${LOGS_DIR}"
    mkdir -p "${BACKUP_DIR}"
    mkdir -p "./ssl"
    
    log_success "Directories created"
}

# Run tests
run_tests() {
    log_info "Running tests..."
    
    if mvn test; then
        log_success "All tests passed"
    else
        log_error "Tests failed"
        exit 1
    fi
}

# Build application
build_application() {
    log_info "Building application..."
    
    if mvn clean package -Pprod; then
        log_success "Application built successfully"
    else
        log_error "Build failed"
        exit 1
    fi
}

# Build Docker image
build_docker_image() {
    log_info "Building Docker image..."
    
    if docker build -t "${DOCKER_IMAGE}" .; then
        log_success "Docker image built successfully"
    else
        log_error "Docker build failed"
        exit 1
    fi
}

# Stop existing containers
stop_containers() {
    log_info "Stopping existing containers..."
    
    if docker-compose down; then
        log_success "Containers stopped"
    else
        log_warning "No containers were running"
    fi
}

# Create backup
create_backup() {
    if [ -d "${DATA_DIR}" ] && [ "$(ls -A ${DATA_DIR})" ]; then
        log_info "Creating backup..."
        
        TIMESTAMP=$(date +%Y%m%d_%H%M%S)
        BACKUP_FILE="${BACKUP_DIR}/backup_${TIMESTAMP}.tar.gz"
        
        if tar -czf "${BACKUP_FILE}" -C "${DATA_DIR}" .; then
            log_success "Backup created: ${BACKUP_FILE}"
        else
            log_error "Backup failed"
            exit 1
        fi
    else
        log_info "No data to backup"
    fi
}

# Generate SSL certificates (self-signed for development)
generate_ssl_certificates() {
    log_info "Generating SSL certificates..."
    
    if [ ! -f "./ssl/cert.pem" ] || [ ! -f "./ssl/key.pem" ]; then
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout ./ssl/key.pem \
            -out ./ssl/cert.pem \
            -subj "/C=US/ST=State/L=City/O=FRC Team/CN=localhost"
        
        log_success "SSL certificates generated"
    else
        log_info "SSL certificates already exist"
    fi
}

# Deploy with Docker Compose
deploy() {
    log_info "Deploying application..."
    
    if docker-compose up -d; then
        log_success "Application deployed successfully"
    else
        log_error "Deployment failed"
        exit 1
    fi
}

# Health check
health_check() {
    log_info "Performing health check..."
    
    # Wait for application to start
    sleep 30
    
    MAX_ATTEMPTS=10
    ATTEMPT=1
    
    while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
        if curl -f http://localhost:8080/actuator/health &> /dev/null; then
            log_success "Application is healthy"
            return 0
        fi
        
        log_info "Health check attempt ${ATTEMPT}/${MAX_ATTEMPTS} failed, retrying in 10 seconds..."
        sleep 10
        ATTEMPT=$((ATTEMPT + 1))
    done
    
    log_error "Health check failed after ${MAX_ATTEMPTS} attempts"
    return 1
}

# Show deployment info
show_deployment_info() {
    log_info "Deployment Information:"
    echo "=========================="
    echo "Application: ${APP_NAME}"
    echo "Version: ${VERSION}"
    echo "Docker Image: ${DOCKER_IMAGE}"
    echo "HTTP URL: http://localhost"
    echo "HTTPS URL: https://localhost"
    echo "Health Check: http://localhost:8080/actuator/health"
    echo "H2 Console: http://localhost:8080/h2-console (development only)"
    echo "Data Directory: ${DATA_DIR}"
    echo "Logs Directory: ${LOGS_DIR}"
    echo "Backup Directory: ${BACKUP_DIR}"
    echo "=========================="
}

# Main deployment process
main() {
    log_info "Starting FRC Project Management System deployment..."
    echo "=================================================="
    
    check_prerequisites
    create_directories
    
    # Create backup before deployment
    create_backup
    
    # Build and test
    run_tests
    build_application
    build_docker_image
    
    # Deploy
    stop_containers
    generate_ssl_certificates
    deploy
    
    # Verify deployment
    health_check
    
    # Show info
    show_deployment_info
    
    log_success "Deployment completed successfully!"
}

# Handle script arguments
case "${1:-deploy}" in
    "deploy")
        main
        ;;
    "stop")
        log_info "Stopping all containers..."
        docker-compose down
        log_success "All containers stopped"
        ;;
    "logs")
        log_info "Showing application logs..."
        docker-compose logs -f frc-project-management
        ;;
    "backup")
        create_backup
        ;;
    "health")
        health_check
        ;;
    "clean")
        log_info "Cleaning up Docker resources..."
        docker-compose down
        docker system prune -f
        log_success "Cleanup completed"
        ;;
    *)
        echo "Usage: $0 {deploy|stop|logs|backup|health|clean}"
        echo ""
        echo "Commands:"
        echo "  deploy  - Full deployment (default)"
        echo "  stop    - Stop all containers"
        echo "  logs    - Show application logs"
        echo "  backup  - Create data backup"
        echo "  health  - Check application health"
        echo "  clean   - Clean up Docker resources"
        exit 1
        ;;
esac