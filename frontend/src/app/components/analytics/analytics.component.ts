import { Component, OnInit, ViewChild, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import { CommonModule } from '@angular/common';
import { LoggerService } from '../../services/logger.service';

Chart.register(...registerables);

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './analytics.component.html',
  styleUrls: ['./analytics.component.css']
})
export class AnalyticsComponent implements OnInit, AfterViewInit, OnDestroy {
  
  @ViewChild('lineChartCanvas') lineChartCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('barChartCanvas') barChartCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('pieChartCanvas') pieChartCanvas!: ElementRef<HTMLCanvasElement>;

  private lineChart: Chart | null = null;
  private barChart: Chart | null = null;
  private pieChart: Chart | null = null;

  salesData: any = {};

  constructor(private http: HttpClient, private logger: LoggerService) {}

  ngOnInit() {
    this.loadAnalytics();
  }

  ngAfterViewInit() {
    // Charts will be initialized after data is loaded
  }

  loadAnalytics() {
    const token = localStorage.getItem('accessToken');
    this.http.get<any>('http://localhost:8080/fast-food-order-api/api/admin/analytics/sales', {
      headers: { 'Authorization': `Bearer ${token}` }
    }).subscribe({
      next: (data) => {
        this.salesData = data;
        this.updateCharts(data);
      },
      error: (err) => {
        this.logger.error('Error loading analytics:', err);
      }
    });
  }

  updateCharts(data: any) {
    // Destroy existing charts if they exist
    if (this.lineChart) {
      this.lineChart.destroy();
    }
    if (this.barChart) {
      this.barChart.destroy();
    }
    if (this.pieChart) {
      this.pieChart.destroy();
    }

    // Create line chart
    if (data.monthlySales && this.lineChartCanvas) {
      const lineCtx = this.lineChartCanvas.nativeElement.getContext('2d');
      if (lineCtx) {
        this.lineChart = new Chart(lineCtx, {
          type: 'line',
          data: {
            labels: data.monthlySales.map((m: any) => m.month),
            datasets: [{
              label: 'Sales',
              data: data.monthlySales.map((m: any) => m.sales),
              borderColor: 'rgb(255, 107, 53)',
              backgroundColor: 'rgba(255, 107, 53, 0.1)',
              tension: 0.4
            }]
          },
          options: {
            responsive: true,
            plugins: {
              legend: {
                display: true,
                position: 'top'
              },
              title: {
                display: true,
                text: 'Monthly Sales Trend'
              }
            }
          }
        });
      }
    }

    // Create bar chart
    if (data.categorySales && this.barChartCanvas) {
      const barCtx = this.barChartCanvas.nativeElement.getContext('2d');
      if (barCtx) {
        this.barChart = new Chart(barCtx, {
          type: 'bar',
          data: {
            labels: data.categorySales.map((c: any) => c.categoryName),
            datasets: [{
              label: 'Sales',
              data: data.categorySales.map((c: any) => c.sales),
              backgroundColor: [
                'rgba(255, 107, 53, 0.8)',
                'rgba(0, 78, 137, 0.8)',
                'rgba(6, 167, 125, 0.8)',
                'rgba(255, 182, 39, 0.8)',
                'rgba(208, 0, 0, 0.8)'
              ]
            }]
          },
          options: {
            responsive: true,
            plugins: {
              legend: {
                display: true,
                position: 'top'
              },
              title: {
                display: true,
                text: 'Category-wise Sales'
              }
            }
          }
        });
      }
    }

    // Create pie chart
    if (data.popularItems && this.pieChartCanvas) {
      const pieCtx = this.pieChartCanvas.nativeElement.getContext('2d');
      if (pieCtx) {
        this.pieChart = new Chart(pieCtx, {
          type: 'pie',
          data: {
            labels: data.popularItems.map((p: any) => p.itemName),
            datasets: [{
              data: data.popularItems.map((p: any) => p.quantitySold),
              backgroundColor: [
                'rgba(255, 107, 53, 0.8)',
                'rgba(0, 78, 137, 0.8)',
                'rgba(6, 167, 125, 0.8)',
                'rgba(255, 182, 39, 0.8)',
                'rgba(208, 0, 0, 0.8)'
              ]
            }]
          },
          options: {
            responsive: true,
            plugins: {
              legend: {
                display: true,
                position: 'right'
              },
              title: {
                display: true,
                text: 'Popular Items'
              }
            }
          }
        });
      }
    }
  }

  ngOnDestroy() {
    if (this.lineChart) {
      this.lineChart.destroy();
    }
    if (this.barChart) {
      this.barChart.destroy();
    }
    if (this.pieChart) {
      this.pieChart.destroy();
    }
  }
}
