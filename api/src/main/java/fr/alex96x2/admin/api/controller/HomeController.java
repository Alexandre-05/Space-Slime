package fr.alex96x2.admin.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body("""
                        <!DOCTYPE html>
                        <html lang="fr">
                        <head>
                          <meta charset="UTF-8" />
                          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                          <title>Space Slime</title>
                          <style>
                            * { box-sizing: border-box; margin: 0; padding: 0; }
                            body {
                              min-height: 100vh;
                              display: flex;
                              align-items: center;
                              justify-content: center;
                              font-family: system-ui, sans-serif;
                              background: linear-gradient(160deg, #0f0a1a 0%, #1a1030 50%, #0d1f2d 100%);
                              color: #e8e0f0;
                            }
                            .card {
                              text-align: center;
                              padding: 3rem 2.5rem;
                              border-radius: 1rem;
                              background: rgba(255,255,255,0.04);
                              border: 1px solid rgba(180,120,255,0.2);
                              max-width: 420px;
                            }
                            h1 { font-size: 1.8rem; margin-bottom: 0.5rem; color: #c9a0ff; }
                            p { color: #9a8fb0; margin-bottom: 2rem; line-height: 1.5; }
                            a {
                              display: inline-block;
                              padding: 0.75rem 1.5rem;
                              border-radius: 0.5rem;
                              background: #7c3aed;
                              color: #fff;
                              text-decoration: none;
                              font-weight: 600;
                            }
                            a:hover { background: #6d28d9; }
                          </style>
                        </head>
                        <body>
                          <div class="card">
                            <h1>Space Slime</h1>
                            <p>Serveur Minecraft — panel d'administration staff</p>
                            <a href="/dashboard/">Accéder au panel</a>
                          </div>
                        </body>
                        </html>
                        """);
    }
}