import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors, HttpInterceptorFn } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { importProvidersFrom } from '@angular/core';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { HttpClient } from '@angular/common/http';

import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { environment } from './environments/environment';
import { resolveApiUrl, RuntimeApiConfig } from './app/core/api-url';

export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

const authInterceptorFn: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    const cloned = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    return next(cloned);
  }
  return next(req);
};

async function loadRuntimeConfig(): Promise<RuntimeApiConfig> {
  try {
    const res = await fetch('assets/config.json', { cache: 'no-store' });
    if (!res.ok) {
      return {};
    }
    return (await res.json()) as RuntimeApiConfig;
  } catch {
    return {};
  }
}

loadRuntimeConfig().then((cfg) => {
  environment.apiUrl = resolveApiUrl(cfg);
  // Helpful for debugging port/host mismatches
  console.info('[api] Using API base URL:', environment.apiUrl);

  return bootstrapApplication(AppComponent, {
    providers: [
      provideRouter(routes),
      provideHttpClient(withInterceptors([authInterceptorFn])),
      provideAnimations(),
      importProvidersFrom(
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useFactory: HttpLoaderFactory,
            deps: [HttpClient]
          },
          defaultLanguage: 'en'
        })
      )
    ]
  });
}).catch(err => {
  console.error('Failed to bootstrap application:', err);
});
