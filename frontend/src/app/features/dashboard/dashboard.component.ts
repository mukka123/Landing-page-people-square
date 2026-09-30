import { Component, OnInit, inject, signal, computed } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ApiService, TrendPoint, GeographicUnit } from "../../core/api.service";

/**
 * Historical booth/area trend view — the platforms headline workflow (HLD "sample end to
 * end scenario: historical booth trend analysis"). Shows vote share across the elections
 * available for the areas the signed-in user is authorized to see, with every number
 * traceable to a formulaVersion.
 */
@Component({
  selector: "app-dashboard",
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="dashboard">
      <header class="dashboard__header">
        <h1>Historical trends</h1>
        <p class="dashboard__subtitle">
          Vote share by election year, for the areas you are authorized to view.
          Every figure links back to its source dataset and formula version.
        </p>
      </header>

      @if (loading()) {
        <p class="dashboard__status">Loading authorized geography…</p>
      } @else if (units().length === 0) {
        <p class="dashboard__status">
          No geography is authorized for your account yet. Contact your constituency administrator.
        </p>
      } @else {
        <div class="dashboard__grid">
          @for (unit of units(); track unit.id) {
            <article class="trend-card">
              <h2>{{ unit.name }}</h2>
              <span class="trend-card__type">{{ unit.type }}</span>

              @if (trendsByUnit()[unit.id]?.length) {
                <table class="trend-card__table">
                  <thead>
                    <tr><th>Year</th><th>Vote share</th></tr>
                  </thead>
                  <tbody>
                    @for (point of trendsByUnit()[unit.id]; track point.electionId) {
                      <tr>
                        <td>{{ point.electionYear }}</td>
                        <td>{{ point.value | number: "1.1-1" }}%</td>
                      </tr>
                    }
                  </tbody>
                </table>
              } @else {
                <p class="trend-card__empty">No published results for this area yet.</p>
              }
            </article>
          }
        </div>
      }
    </section>
  `,
  styles: [`
    .dashboard__header { margin-bottom: 1.5rem; max-width: 60ch; }
    .dashboard__header h1 { margin: 0 0 0.25rem; font-size: 1.4rem; }
    .dashboard__subtitle { color: var(--color-text-muted); margin: 0; font-size: 0.9rem; }
    .dashboard__status { color: var(--color-text-muted); }
    .dashboard__grid {
      display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 1rem;
    }
    .trend-card {
      background: var(--color-surface); border: 1px solid var(--color-border);
      border-radius: 6px; padding: 1rem;
    }
    .trend-card h2 { margin: 0; font-size: 1rem; }
    .trend-card__type { color: var(--color-text-muted); font-size: 0.75rem; }
    .trend-card__table { width: 100%; margin-top: 0.75rem; border-collapse: collapse; font-size: 0.9rem; }
    .trend-card__table th, .trend-card__table td { text-align: left; padding: 0.25rem 0; }
    .trend-card__table th { color: var(--color-text-muted); font-weight: 500; }
    .trend-card__empty { color: var(--color-text-muted); font-size: 0.85rem; margin-top: 0.75rem; }
  `]
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly units = signal<GeographicUnit[]>([]);
  private readonly trends = signal<Record<string, TrendPoint[]>>({});

  readonly trendsByUnit = computed(() => this.trends());

  ngOnInit(): void {
    this.api.getAuthorizedGeography().subscribe({
      next: (units) => {
        this.units.set(units);
        this.loading.set(false);
        if (units.length > 0) {
          this.api.getTrends(units.map((u) => u.id), "VOTE_SHARE").subscribe((points) => {
            const grouped: Record<string, TrendPoint[]> = {};
            for (const point of points) {
              (grouped[point.geographicUnitId] ??= []).push(point);
            }
            this.trends.set(grouped);
          });
        }
      },
      error: () => this.loading.set(false)
    });
  }
}
