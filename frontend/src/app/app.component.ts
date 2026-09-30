import { Component } from "@angular/core";
import { RouterOutlet, RouterLink } from "@angular/router";

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <div class="shell">
      <header class="shell__header">
        <span class="shell__brand">Constituency Analytics</span>
        <nav class="shell__nav">
          <a routerLink="/">Dashboard</a>
          <a routerLink="/geography">Geography</a>
        </nav>
      </header>
      <main class="shell__main">
        <router-outlet />
      </main>
    </div>
  `,
  styles: [`
    .shell { display: flex; flex-direction: column; min-height: 100vh; }
    .shell__header {
      display: flex; align-items: center; justify-content: space-between;
      padding: 0.75rem 1.5rem; border-bottom: 1px solid var(--color-border);
      background: var(--color-surface);
    }
    .shell__brand { font-weight: 600; letter-spacing: 0.01em; }
    .shell__nav { display: flex; gap: 1.25rem; }
    .shell__nav a { color: var(--color-text-muted); text-decoration: none; font-size: 0.9rem; }
    .shell__nav a:hover { color: var(--color-text); }
    .shell__main { flex: 1; padding: 1.5rem; }
  `]
})
export class AppComponent {}
