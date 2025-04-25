// src/main/resources/web/gantt-chart.js

// Global functions used by the HTML page
let chart = null;
let chartData = null;
let currentViewMode = 'week';
let showingMilestones = true;
let showingDependencies = true;

// Initialize the chart
function initializeChart() {
    const ctx = document.getElementById('gantt-chart').getContext('2d');
    
    chart = new Chart(ctx, {
        type: 'bar',
        data: {
            datasets: []
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            indexAxis: 'y',
            scales: {
                x: {
                    type: 'time',
                    position: 'top',
                    time: {
                        unit: 'day',
                        displayFormats: {
                            day: 'MMM D'
                        }
                    },
                    grid: {
                        color: '#e0e0e0'
                    }
                },
                y: {
                    beginAtZero: true,
                    grid: {
                        display: false
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        title: function(context) {
                            return context[0].dataset.label;
                        },
                        label: function(context) {
                            const task = context.dataset.taskData;
                            if (!task) return [];
                            
                            const startDate = moment(task.startDate).format('MMM D, YYYY');
                            const endDate = moment(task.endDate).format('MMM D, YYYY');
                            const result = [
                                `Start: ${startDate}`,
                                `End: ${endDate}`,
                                `Progress: ${task.progress}%`
                            ];
                            
                            if (task.assignee) {
                                result.push(`Assigned to: ${task.assignee}`);
                            }
                            
                            if (task.subsystem) {
                                result.push(`Subsystem: ${task.subsystem}`);
                            }
                            
                            return result;
                        }
                    },
                    displayColors: false
                },
                legend: {
                    display: false
                }
            },
            onClick: handleChartClick
        }
    });
    
    // Notify Java that the chart is ready
    if (typeof javaBridge !== 'undefined') {
        javaBridge.onChartReady();
    }
}

// Update chart data with the provided data
function updateChartData(data) {
    try {
        chartData = data;
        
        if (!chart) {
            console.error('Chart not initialized');
            return;
        }
        
        // Clear current datasets
        chart.data.datasets = [];
        chart.options.scales.y.labels = [];
        
        // Process tasks if available
        if (data.tasks && data.tasks.length > 0) {
            processTasks(data.tasks);
        }
        
        // Process milestones if enabled and available
        if (showingMilestones && data.milestones && data.milestones.length > 0) {
            processMilestones(data.milestones);
        }
        
        // Process dependencies if enabled and available
        if (showingDependencies && data.dependencies && data.dependencies.length > 0) {
            processDependencies(data.dependencies);
        }
        
        // Update chart date range if specified
        if (data.startDate && data.endDate) {
            const startDate = moment(data.startDate);
            const endDate = moment(data.endDate);
            
            chart.options.scales.x.min = startDate.valueOf();
            chart.options.scales.x.max = endDate.valueOf();
        }
        
        // Update chart
        chart.update();
        
        console.log('Chart data updated');
        if (typeof javaBridge !== 'undefined') {
            javaBridge.log('Chart data updated');
        }
    } catch (error) {
        console.error('Error updating chart data:', error);
        if (typeof javaBridge !== 'undefined') {
            javaBridge.logError('Error updating chart data: ' + error.toString());
        }
    }
}

// Process tasks for chart display
function processTasks(tasks) {
    if (!tasks || tasks.length === 0) return;
    
    // Create a reverse map for y-axis positioning
    const taskLabels = tasks.map(task => task.title);
    chart.options.scales.y.labels = taskLabels;
    
    // Add each task as a dataset
    tasks.forEach((task, index) => {
        const startDate = moment(task.startDate).valueOf();
        const endDate = moment(task.endDate).valueOf();
        
        const dataset = {
            label: task.title,
            taskData: task, // Store original task data for tooltip and click handling
            backgroundColor: task.color || getColorForProgress(task.progress),
            borderColor: getDarkerColor(task.color || getColorForProgress(task.progress)),
            borderWidth: 1,
            borderRadius: 4,
            barPercentage: 0.7,
            categoryPercentage: 0.8,
            data: [{
                x: [startDate, endDate],
                y: index
            }]
        };
        
        chart.data.datasets.push(dataset);
    });
}

// Process milestones for chart display
function processMilestones(milestones) {
    if (!milestones || milestones.length === 0) return;
    
    // Create annotations for milestones
    const annotations = {};
    
    milestones.forEach((milestone, index) => {
        const milestoneDate = moment(milestone.startDate).valueOf(); // Milestones have same start and end date
        
        annotations[`milestone-${index}`] = {
            type: 'line',
            borderColor: milestone.color || '#9c27b0', // Purple for milestones by default
            borderWidth: 2,
            borderDash: [5, 5],
            value: milestoneDate,
            scaleID: 'x',
            label: {
                enabled: true,
                content: milestone.title,
                position: 'top',
                backgroundColor: milestone.color || '#9c27b0',
                color: 'white',
                font: {
                    weight: 'bold',
                    size: 10
                }
            }
        };
    });
    
    // Set annotations plugin options
    chart.options.plugins.annotation = {
        annotations: annotations
    };
}

// Process dependencies for chart display
function processDependencies(dependencies) {
    if (!dependencies || dependencies.length === 0) return;
    
    // This requires a custom drawing implementation which is beyond
    // the scope of Chart.js's built-in features
    console.log('Dependencies processing not fully implemented yet');
    
    // In a future implementation, we could:
    // 1. Create a custom plugin for Chart.js
    // 2. Draw dependency arrows directly on the canvas using Chart.js afterDraw hook
}

// Get color based on progress
function getColorForProgress(progress) {
    if (progress >= 100) {
        return 'rgba(76, 175, 80, 0.7)'; // Green for completed
    } else if (progress >= 75) {
        return 'rgba(33, 150, 243, 0.7)'; // Blue for almost done
    } else if (progress >= 25) {
        return 'rgba(255, 152, 0, 0.7)'; // Orange for in progress
    } else {
        return 'rgba(244, 67, 54, 0.7)'; // Red for just started
    }
}

// Get darker version of a color for borders
function getDarkerColor(color) {
    // Simple implementation - in a real app, would use a color library
    return color.replace('0.7', '1.0');
}

// Handle click on chart elements
function handleChartClick(event, elements) {
    if (!elements || elements.length === 0) return;
    
    try {
        const element = elements[0];
        const dataset = chart.data.datasets[element.datasetIndex];
        
        // If this is a task dataset with taskData
        if (dataset && dataset.taskData) {
            const taskId = dataset.taskData.id;
            console.log('Task clicked:', taskId);
            
            // Notify Java via bridge
            if (typeof javaBridge !== 'undefined') {
                javaBridge.onTaskSelected(taskId);
            }
        }
    } catch (error) {
        console.error('Error handling chart click:', error);
        if (typeof javaBridge !== 'undefined') {
            javaBridge.logError('Error handling chart click: ' + error.toString());
        }
    }
}

// Set view mode (day, week, month)
function setViewMode(mode) {
    try {
        currentViewMode = mode.toLowerCase();
        
        // Update time unit based on view mode
        let unit = 'day';
        let stepSize = 1;
        
        switch (currentViewMode) {
            case 'day':
                unit = 'hour';
                stepSize = 4;
                break;
            case 'week':
                unit = 'day';
                stepSize = 1;
                break;
            case 'month':
                unit = 'week';
                stepSize = 1;
                break;
        }
        
        // Update chart options
        chart.options.scales.x.time.unit = unit;
        chart.options.scales.x.ticks.stepSize = stepSize;
        
        // Update chart
        chart.update();
        
        console.log('View mode set to', mode);
    } catch (error) {
        console.error('Error setting view mode:', error);
        if (typeof javaBridge !== 'undefined') {
            javaBridge.logError('Error setting view mode: ' + error.toString());
        }
    }
}

// Set milestones visibility
function setMilestonesVisibility(visible) {
    try {
        showingMilestones = visible;
        
        // Update chart with current data to apply visibility change
        if (chartData) {
            updateChartData(chartData);
        }
        
        console.log('Milestones visibility set to', visible);
    } catch (error) {
        console.error('Error setting milestones visibility:', error);
        if (typeof javaBridge !== 'undefined') {
            javaBridge.logError('Error setting milestones visibility: ' + error.toString());
        }
    }
}

// Set dependencies visibility
function setDependenciesVisibility(visible) {
    try {
        showingDependencies = visible;
        
        // Update chart with current data to apply visibility change
        if (chartData) {
            updateChartData(chartData);
        }
        
        console.log('Dependencies visibility set to', visible);
    } catch (error) {
        console.error('Error setting dependencies visibility:', error);
        if (typeof javaBridge !== 'undefined') {
            javaBridge.logError('Error setting dependencies visibility: ' + error.toString());
        }
    }
}

// Export chart as an image (PNG)
function exportChart(format) {
    try {
        if (!chart) {
            console.error('Chart not initialized');
            return null;
        }
        
        if (format.toLowerCase() === 'png') {
            return chart.toBase64Image();
        }
        
        console.error('Unsupported export format:', format);
        return null;
    } catch (error) {
        console.error('Error exporting chart:', error);
        if (typeof javaBridge !== 'undefined') {
            javaBridge.logError('Error exporting chart: ' + error.toString());
        }
        return null;
    }
}

// Initialize when document is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeChart();
});