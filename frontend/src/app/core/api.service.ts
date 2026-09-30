import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

export interface TrendPoint {
  electionId: string;
  electionYear: number;
  geographicUnitId: string;
  partyId: string | null;
  value: number;
  formulaVersion: string;
}

export interface GeographicUnit {
  id: string;
  type: string;
  name: string;
  parentId: string | null;
}

/** Thin typed wrapper over the backend REST API — no business logic lives here. */
@Injectable({ providedIn: "root" })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  getTrends(geographicUnitIds: string[], metric: string): Observable<TrendPoint[]> {
    let params = new HttpParams().set("metric", metric);
    for (const id of geographicUnitIds) {
      params = params.append("geographicUnitIds", id);
    }
    return this.http.get<TrendPoint[]>(`${this.base}/analytics/trends`, { params });
  }

  getAuthorizedGeography(): Observable<GeographicUnit[]> {
    return this.http.get<GeographicUnit[]>(`${this.base}/geography/units`);
  }
}
