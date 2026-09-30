import { Component, OnInit, inject, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ApiService, GeographicUnit } from "../../core/api.service";

/** Placeholder for the geography/crosswalk review screen — Phase One scope, not yet built. */
@Component({
  selector: "app-geography",
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1>Geography</h1>
    <p>Boundary and polling-station crosswalk review UI goes here (EPIC 02 / EPIC 04).</p>
    <ul>
      @for (unit of units(); track unit.id) {
        <li>{{ unit.name }} ({{ unit.type }})</li>
      }
    </ul>
  `
})
export class GeographyComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly units = signal<GeographicUnit[]>([]);

  ngOnInit(): void {
    this.api.getAuthorizedGeography().subscribe((units) => this.units.set(units));
  }
}
