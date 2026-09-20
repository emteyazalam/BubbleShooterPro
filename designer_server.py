import json
import os
import sys
from http.server import HTTPServer, SimpleHTTPRequestHandler

class DesignerHandler(SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(200)
        self.end_headers()

    def do_POST(self):
        if self.path == '/api/save-level':
            content_length = int(self.headers.get('Content-Length', 0))
            body = self.rfile.read(content_length).decode('utf-8')
            try:
                data = json.loads(body)
                level_num = int(data.get('level', 1))
                filename = f"level_{level_num:02d}.json" if level_num < 100 else f"level_{level_num}.json"
                base_dir = os.path.dirname(os.path.abspath(__file__))
                target_path = os.path.join(base_dir, "BubbleShooterpro", "app", "src", "main", "assets", "levels", filename)
                os.makedirs(os.path.dirname(target_path), exist_ok=True)
                with open(target_path, 'w', encoding='utf-8') as f:
                    json.dump(data, f, indent=2)

                self.send_response(200)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                self.wfile.write(json.dumps({'success': True, 'file': filename, 'level': level_num, 'path': target_path}).encode('utf-8'))
            except Exception as e:
                self.send_response(500)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                self.wfile.write(json.dumps({'success': False, 'error': str(e)}).encode('utf-8'))
        else:
            self.send_error(404, "Not Found")

def run(port=8086):
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    server_address = ('', port)
    httpd = HTTPServer(server_address, DesignerHandler)
    print(f"Bubble Shooter Pro Tool Server running on http://localhost:{port}")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down server...")
        httpd.server_close()

if __name__ == '__main__':
    p = int(sys.argv[1]) if len(sys.argv) > 1 else 8086
    run(p)
