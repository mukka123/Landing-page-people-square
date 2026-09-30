import { Routes } from "@angular/router";

export const routes: Routes = [
  {
    path: "",
    loadComponent: () =>
      import("./features/dashboard/dashboard.component").then((m) => m.DashboardComponent)
  },
  {
    path: "geography",
    loadComponent: () =>
      import("./features/geography/geography.component").then((m) => m.GeographyComponent)
  },
  { path: "**", redirectTo: "" }
];
