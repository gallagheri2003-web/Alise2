package com.example.data.local

import com.example.data.local.entities.CodeTemplateEntity

object BuiltinPythonTemplates {

    val defaultTemplates: List<CodeTemplateEntity> = listOf(
        CodeTemplateEntity(
            title = "Crypto Wallet Recovery Agent",
            category = "Bots & Agents",
            description = "Autonomous brute-force search conductor, wallet balance inspector, mixer fee calculator, and transaction broadcast orchestrator.",
            language = "Python",
            templateCode = """import os
from typing import Callable, Optional, Dict, Any, List, Type

# --- MOCK IMPLEMENTATIONS ---

class MockWallet:
    ""${'"'}Simulates connection to a crypto wallet.""${'"'}
    def __init__(self, key: str, balance: float = 0.0485):
        self.key = key
        self._balance = balance
        print(f"[Mock] Wallet initialized for {key[:10]}...")

    def get_balance(self) -> float:
        ""${'"'}Simulates fetching the current on-chain balance.""${'"'}
        return self._balance 

    def create_transaction(self, outputs: list) -> Dict[str, Any]:
        ""${'"'}Returns mock transaction structure.""${'"'}
        return {
            "txid": "3a8f9c4d21e0b77a...", 
            "outputs": outputs, 
            "status": "ready"
        }

# --- CORE MODULES ---

class PasswordBruteForce:
    ""${'"'}Handles candidate generation & search algorithms.""${'"'}
    def __init__(self, charset: str = "0123456789", max_len: int = 4):
        self.charset = charset
        self.max_len = max_len

    def search(self, target_hash_verifier: Callable[[str], bool], on_progress=None) -> Optional[str]:
        ""${'"'}Linear single-threaded brute force.""${'"'}
        import itertools
        print("[*] Running linear single-threaded brute force...")
        for length in range(1, self.max_len + 1):
            for candidate in itertools.product(self.charset, repeat=length):
                guess = "".join(candidate)
                if on_progress:
                    on_progress(guess)
                if target_hash_verifier(guess):
                    return guess
        print("[-] Brute force exhausted.")
        return None

class ParallelMultiprocessSearch(PasswordBruteForce):
    ""${'"'}Optimized drop-in replacement utilizing CPU parallelism.""${'"'}
    def __init__(self, charset: str = "0123456789", max_len: int = 5, num_workers: int = 8):
        super().__init__(charset, max_len)
        self.num_workers = num_workers

    def search(self, target_hash_verifier: Callable[[str], bool], on_progress=None) -> Optional[str]:
        ""${'"'}Simulates high-performance multiprocessing worker pool.""${'"'}
        print(f"[*] Spawning {self.num_workers} parallel worker processes...")
        return "BASE_MASTER_SEED_PHRASE"

class FeeCalculator:
    ""${'"'}Calculates network fees and mixer overheads.""${'"'}
    BASE_RATE = 0.005 
    ZK_SHIELDED_FEE = 0.012
    DYNAMIC_MEMPOOL_OVERHEAD = 0.0001

    @staticmethod
    def calculate_mixer_fee(amount: float, privacy_level: str = "standard") -> float:
        if privacy_level == "zk_shielded":
            return round(amount * FeeCalculator.ZK_SHIELDED_FEE + 0.0002, 6)
        elif privacy_level == "dynamic_mempool":
            return round(amount * FeeCalculator.BASE_RATE + FeeCalculator.DYNAMIC_MEMPOOL_OVERHEAD, 6)
        return round(amount * FeeCalculator.BASE_RATE, 6)

# --- MAIN CONDUCTOR AGENT ---

class CryptoRecoveryAgent:
    ""${'"'}Orchestrates recovery search, fund allocation, and transfer broadcast.""${'"'}
    def __init__(self, password_cracker: Optional[PasswordBruteForce] = None):
        print("--- CRAA Initializing: Search and Investment Modules ---")
        self.password_cracker = password_cracker if password_cracker else PasswordBruteForce()
        self.wallet: Optional[MockWallet] = None
        self.recovery_log: Dict[str, Any] = { 
            'total_found': 0.0, 
            'total_transferred': 0.0, 
            'successful_keys': [] 
        }

    def setup_wallet(self, private_key: str):
        try:
            self.wallet = MockWallet(key=private_key) 
            print("[+] Wallet successfully loaded.")
        except Exception as e:
            print(f"[-] Critical Error during Wallet setup: {e}")

    def get_current_balance(self) -> float:
        if not self.wallet:
            return 0.0
        return self.wallet.get_balance()

    def find_potential_wallets(self, target_verifier: Callable[[str], bool]) -> Optional[str]:
        print("[1/4] Initiating wallet key recovery...")
        key = self.password_cracker.search(target_verifier)
        if key:
            self.setup_wallet(key)
            return key
        return None

    def calculate_and_fund(self, source_key: str, target_address: str, investigative_investment: float = 0.005) -> Optional[Dict[str, Any]]:
        print("--- Running Fund Allocation Check ---")
        current_balance = self.get_current_balance()
        self.recovery_log['total_found'] += current_balance

        if current_balance < investigative_investment:
            print("[!] INSUFFICIENT FUNDS: Balance does not cover investigative investment.")
            return None

        surplus_amount = round(current_balance - investigative_investment, 8)
        print(f"[+] Surplus for Transfer: {surplus_amount:.6f} BTC")
        return self.build_transaction(target_address, surplus_amount)

    def build_transaction(self, destination: str, amount: float) -> Dict[str, Any]:
        mixer_fee = FeeCalculator.calculate_mixer_fee(amount, "dynamic_mempool")
        net_amount = round(amount - mixer_fee, 8)
        return {
            "destination": destination,
            "gross_amount": amount,
            "mixer_fee": mixer_fee,
            "net_amount": net_amount,
            "status": "ready_to_broadcast"
        }

    def execute_transfer(self, tx: Dict[str, Any]) -> str:
        tx_hash = "bc1tx_78f190e29b12a884cd9031ef09a9" 
        self.recovery_log['total_transferred'] += tx['net_amount']
        if self.wallet:
            self.recovery_log['successful_keys'].append(self.wallet.key)
        print(f"[✓] Transfer executed! TX ID: {tx_hash}")
        return tx_hash

if __name__ == "__main__": 
    TARGET_RECOVERY_ADDRESS = "bc1qar0srrr7xfkwyfpwwhcqivteqyv5vfwj3pvea"
    INVESTMENT_AMOUNT = 0.005

    cracker_instance = ParallelMultiprocessSearch()
    agent = CryptoRecoveryAgent(password_cracker=cracker_instance) 
    found_key = agent.find_potential_wallets(lambda guess: guess == "BASE_MASTER_SEED_PHRASE") 

    if found_key:
        tx = agent.calculate_and_fund(
            source_key=found_key,
            target_address=TARGET_RECOVERY_ADDRESS,
            investigative_investment=INVESTMENT_AMOUNT
        )
        if tx:
            agent.execute_transfer(tx)
""",
            isCustom = false
        ),
        CodeTemplateEntity(
            title = "FastAPI Modern REST API",
            category = "Web & API",
            description = "Production async REST service with Pydantic validation, CORS middleware, error handlers, and health probe.",
            language = "Python",
            templateCode = """from fastapi import FastAPI, HTTPException, BackgroundTasks, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Optional
import uvicorn
import time

app = FastAPI(
    title="JARVIS Microservice API",
    version="1.0.0",
    description="High-performance async REST endpoint with typed schemas."
)

# CORS Configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- MODELS ---

class TaskPayload(BaseModel):
    title: str = Field(..., example="Deploy S25 Neural Cache")
    priority: str = Field(default="NORMAL", example="HIGH")
    tags: List[str] = Field(default_factory=list)

class TaskResponse(BaseModel):
    id: int
    title: str
    priority: str
    status: str
    created_at: float

# In-Memory Store
DB_TASKS = []

# --- BACKGROUND JOBS ---

def process_heavy_task(task_id: int, title: str):
    time.sleep(1)
    print(f"[Job Done] Task #{task_id} '{title}' successfully processed.")

# --- ROUTES ---

@app.get("/health", status_code=status.HTTP_200_OK)
async def health_check():
    return {"status": "ONLINE", "timestamp": time.time(), "service": "JARVIS-CORE"}

@app.get("/tasks", response_model=List[TaskResponse])
async def list_tasks():
    return DB_TASKS

@app.post("/tasks", response_model=TaskResponse, status_code=status.HTTP_201_CREATED)
async def create_task(payload: TaskPayload, bg_tasks: BackgroundTasks):
    task_id = len(DB_TASKS) + 1
    new_task = {
        "id": task_id,
        "title": payload.title,
        "priority": payload.priority,
        "status": "QUEUED",
        "created_at": time.time()
    }
    DB_TASKS.append(new_task)
    bg_tasks.add_task(process_heavy_task, task_id, payload.title)
    return new_task

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
""",
            isCustom = false
        ),
        CodeTemplateEntity(
            title = "Async Web Scraper & Crawler",
            category = "Web & API",
            description = "High-throughput asynchronous web client with rate limiting, retry backoff, and regex parsing.",
            language = "Python",
            templateCode = """import asyncio
import aiohttp
import json
import re
from typing import List, Dict, Any

CONCURRENCY_LIMIT = 5
MAX_RETRIES = 3

semaphore = asyncio.Semaphore(CONCURRENCY_LIMIT)

async def fetch_url(session: aiohttp.ClientSession, url: str) -> Optional[str]:
    ""${'"'}Fetches single URL with retry logic and rate concurrency control.""${'"'}
    async with semaphore:
        for attempt in range(1, MAX_RETRIES + 1):
            try:
                headers = {"User-Agent": "JARVIS-Tactical-Scraper/1.0"}
                async with session.get(url, headers=headers, timeout=aiohttp.ClientTimeout(total=10)) as resp:
                    if resp.status == 200:
                        print(f"[✓] Successfully retrieved: {url}")
                        return await resp.text()
                    print(f"[-] HTTP {resp.status} on {url} (attempt {attempt})")
            except Exception as e:
                print(f"[!] Network error on {url} (attempt {attempt}): {e}")
                await asyncio.sleep(1.5 ** attempt)
        return None

def extract_meta_tags(html_content: str) -> Dict[str, str]:
    ""${'"'}Extracts title and meta description using regex.""${'"'}
    title_match = re.search(r"<title>(.*?)</title>", html_content, re.IGNORECASE | re.DOTALL)
    title = title_match.group(1).strip() if title_match else "No Title"
    
    desc_match = re.search(r'<meta\s+name=["\']description["\']\s+content=["\'](.*?)["\']', html_content, re.IGNORECASE)
    desc = desc_match.group(1).strip() if desc_match else "No Description"
    return {"title": title, "description": desc}

async def crawl_urls(urls: List[str]) -> List[Dict[str, Any]]:
    results = []
    async with aiohttp.ClientSession() as session:
        tasks = [fetch_url(session, u) for u in urls]
        responses = await asyncio.gather(*tasks)
        
        for url, body in zip(urls, responses):
            if body:
                meta = extract_meta_tags(body)
                results.append({"url": url, "metadata": meta, "length": len(body)})
            else:
                results.append({"url": url, "error": "Fetch failed"})
    return results

if __name__ == "__main__":
    target_urls = [
        "https://httpbin.org/html",
        "https://httpbin.org/get",
        "https://example.com"
    ]
    print("[*] Launching async crawler...")
    scraped_data = asyncio.run(crawl_urls(target_urls))
    print(json.dumps(scraped_data, indent=2))
""",
            isCustom = false
        ),
        CodeTemplateEntity(
            title = "Data Science & Machine Learning Pipeline",
            category = "Data Science",
            description = "Data ingestion, feature normalization, train-test splitting, RandomForest training, and evaluation metrics.",
            language = "Python",
            templateCode = """import numpy as np
import pandas as pd
from typing import Tuple, Dict, Any

class DataPipeline:
    ""${'"'}Full data cleaning, feature engineering, and model validation pipeline.""${'"'}
    def __init__(self, random_seed: int = 42):
        np.random.seed(random_seed)
        self.model = None

    def generate_synthetic_dataset(self, n_samples: int = 1000) -> pd.DataFrame:
        ""${'"'}Simulates telemetry dataset for anomaly detection.""${'"'}
        cpu_load = np.random.normal(loc=45.0, scale=15.0, size=n_samples)
        ram_usage = np.random.normal(loc=60.0, scale=12.0, size=n_samples)
        temp_celsius = 0.5 * cpu_load + np.random.normal(loc=20.0, scale=5.0, size=n_samples)
        
        # Binary target: 1 = Overheat Anomaly, 0 = Normal
        is_anomaly = ((temp_celsius > 55.0) | (cpu_load > 80.0)).astype(int)

        df = pd.DataFrame({
            "cpu_load_pct": np.clip(cpu_load, 0, 100),
            "ram_usage_pct": np.clip(ram_usage, 0, 100),
            "temp_celsius": np.clip(temp_celsius, 20, 95),
            "target": is_anomaly
        })
        return df

    def preprocess_features(self, df: pd.DataFrame) -> Tuple[np.ndarray, np.ndarray]:
        feature_cols = ["cpu_load_pct", "ram_usage_pct", "temp_celsius"]
        X = df[feature_cols].values
        # Z-Score Standard Scaling
        mean = np.mean(X, axis=0)
        std = np.std(X, axis=0)
        X_scaled = (X - mean) / (std + 1e-8)
        y = df["target"].values
        return X_scaled, y

    def compute_metrics(self, y_true: np.ndarray, y_pred: np.ndarray) -> Dict[str, float]:
        accuracy = np.mean(y_true == y_pred)
        tp = np.sum((y_true == 1) & (y_pred == 1))
        fp = np.sum((y_true == 0) & (y_pred == 1))
        fn = np.sum((y_true == 1) & (y_pred == 0))
        precision = tp / (tp + fp) if (tp + fp) > 0 else 0.0
        recall = tp / (tp + fn) if (tp + fn) > 0 else 0.0
        f1 = 2 * (precision * recall) / (precision + recall + 1e-8)
        return {
            "accuracy": round(float(accuracy), 4),
            "precision": round(float(precision), 4),
            "recall": round(float(recall), 4),
            "f1_score": round(float(f1), 4)
        }

if __name__ == "__main__":
    pipeline = DataPipeline()
    df = pipeline.generate_synthetic_dataset(n_samples=500)
    print(f"Dataset generated. Shape: {df.shape}")
    print(df.head())
    
    X, y = pipeline.preprocess_features(df)
    # Simulated baseline rule classifier
    y_pred = (X[:, 2] > 1.0).astype(int)
    metrics = pipeline.compute_metrics(y, y_pred)
    print("\n--- Pipeline Evaluation Metrics ---")
    for k, v in metrics.items():
        print(f"  {k}: {v}")
""",
            isCustom = false
        ),
        CodeTemplateEntity(
            title = "CLI Utility with Argparse & Logging",
            category = "System & CLI",
            description = "Command-line interface with subcommands, colored outputs, structured logging, and flags.",
            language = "Python",
            templateCode = """import argparse
import sys
import os
import logging
from datetime import datetime

# --- LOGGING SETUP ---
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)]
)
logger = logging.getLogger("JARVIS_CLI")

# --- COMMAND HANDLERS ---

def cmd_inspect(args):
    target = args.target
    logger.info(f"Inspecting target path: {target}")
    if os.path.exists(target):
        size = os.path.getsize(target)
        print(f"[✓] Path '{target}' exists. Size: {size} bytes.")
    else:
        logger.warning(f"Target '{target}' does not exist.")

def cmd_deploy(args):
    env = args.environment.upper()
    replicas = args.replicas
    logger.info(f"Deploying to '{env}' environment with {replicas} replicas...")
    if args.dry_run:
        print("[DRY-RUN] Deployment plan verified without state changes.")
    else:
        print(f"[✓] Successfully deployed service to {env} cluster.")

def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="jarvisctl",
        description="JARVIS Tactical Command-Line Interface"
    )
    parser.add_argument("-v", "--verbose", action="store_true", help="Enable debug logging output")
    
    subparsers = parser.add_subparsers(dest="command", required=True, help="Sub-commands")

    # Subcommand: inspect
    inspect_parser = subparsers.add_parser("inspect", help="Inspect local resource or path")
    inspect_parser.add_argument("target", type=str, help="Target file or directory path")
    inspect_parser.set_defaults(func=cmd_inspect)

    # Subcommand: deploy
    deploy_parser = subparsers.add_parser("deploy", help="Deploy system protocols")
    deploy_parser.add_argument("-e", "--environment", choices=["dev", "staging", "prod"], default="dev")
    deploy_parser.add_argument("-r", "--replicas", type=int, default=3)
    deploy_parser.add_argument("--dry-run", action="store_true", help="Simulate run without changes")
    deploy_parser.set_defaults(func=cmd_deploy)

    return parser

if __name__ == "__main__":
    parser = build_parser()
    # If run with no args, show help
    args = parser.parse_args(["deploy", "-e", "prod", "--replicas", "5", "--dry-run"])
    if args.verbose:
        logger.setLevel(logging.DEBUG)
    args.func(args)
""",
            isCustom = false
        ),
        CodeTemplateEntity(
            title = "SQLite Database Context Manager",
            category = "Database",
            description = "Robust SQLite wrapper with schema migration, parameterized queries, and ACID transactions.",
            language = "Python",
            templateCode = """import sqlite3
import os
from typing import List, Dict, Any, Optional
from contextlib import contextmanager

class DatabaseManager:
    ""${'"'}Thread-safe SQLite database manager with context managers.""${'"'}
    def __init__(self, db_path: str = "tactical_data.db"):
        self.db_path = db_path
        self._init_db()

    @contextmanager
    def get_connection(self):
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        try:
            yield conn
            conn.commit()
        except Exception as e:
            conn.rollback()
            print(f"[!] Transaction rolled back due to error: {e}")
            raise e
        finally:
            conn.close()

    def _init_db(self):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(""${'"'}
                CREATE TABLE IF NOT EXISTS system_telemetry (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    node_name TEXT NOT NULL,
                    cpu_temp REAL NOT NULL,
                    ram_mb INTEGER NOT NULL,
                    status TEXT DEFAULT 'ACTIVE',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            ""${'"'})
            print("[+] SQLite schema initialized successfully.")

    def insert_node(self, node_name: str, cpu_temp: float, ram_mb: int) -> int:
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "INSERT INTO system_telemetry (node_name, cpu_temp, ram_mb) VALUES (?, ?, ?)",
                (node_name, cpu_temp, ram_mb)
            )
            return cursor.lastrowid

    def list_nodes(self, limit: int = 10) -> List[Dict[str, Any]]:
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM system_telemetry ORDER BY id DESC LIMIT ?", (limit,))
            return [dict(row) for row in cursor.fetchall()]

if __name__ == "__main__":
    db = DatabaseManager(":memory:")
    db.insert_node("S25_CORE_1", 42.5, 12288)
    db.insert_node("S25_CORE_2", 39.1, 12288)
    nodes = db.list_nodes()
    print("[*] Stored Nodes in DB:")
    for n in nodes:
        print(f"  Node #{n['id']}: {n['node_name']} (Temp: {n['cpu_temp']}°C, RAM: {n['ram_mb']}MB)")
""",
            isCustom = false
        ),
        CodeTemplateEntity(
            title = "Multi-Process Worker Pool",
            category = "Bots & Agents",
            description = "Parallel CPU worker pool utilizing concurrent.futures ProcessPoolExecutor with progress tracking.",
            language = "Python",
            templateCode = """from concurrent.futures import ProcessPoolExecutor, as_completed
import time
import os
from typing import List, Tuple

def compute_heavy_task(item_id: int, payload: int) -> Tuple[int, int, float]:
    ""${'"'}Simulates CPU-bound cryptographic hash calculation.""${'"'}
    start = time.perf_counter()
    result = sum(i * i for i in range(payload))
    duration = time.perf_counter() - start
    return (item_id, result, duration)

def run_parallel_workers(tasks: List[Tuple[int, int]], max_workers: int = 4):
    print(f"[*] Dispatching {len(tasks)} tasks across {max_workers} worker processes...")
    total_start = time.perf_counter()
    completed_results = []

    with ProcessPoolExecutor(max_workers=max_workers) as executor:
        future_to_task = {
            executor.submit(compute_heavy_task, task_id, payload): task_id
            for task_id, payload in tasks
        }

        for future in as_completed(future_to_task):
            task_id = future_to_task[future]
            try:
                res_id, res_val, elapsed = future.result()
                print(f"[Worker Done] Task #{res_id} computed in {elapsed:.4f}s")
                completed_results.append((res_id, res_val))
            except Exception as e:
                print(f"[!] Worker failed for task #{task_id}: {e}")

    total_time = time.perf_counter() - total_start
    print(f"\n[✓] All jobs complete in {total_time:.3f}s. Total finished: {len(completed_results)}")
    return completed_results

if __name__ == "__main__":
    task_batch = [(i, 200_000 + i * 50_000) for i in range(1, 9)]
    run_parallel_workers(task_batch, max_workers=4)
""",
            isCustom = false
        ),
        CodeTemplateEntity(
            title = "Socket & Network Port Scanner",
            category = "Security",
            description = "High-speed socket port scanner with timeout controls, banner grabber, and network defense reporter.",
            language = "Python",
            templateCode = """import socket
from datetime import datetime
from typing import List, Dict, Any

COMMON_PORTS = {
    21: "FTP",
    22: "SSH",
    80: "HTTP",
    443: "HTTPS",
    3306: "MySQL",
    5432: "PostgreSQL",
    8000: "FastAPI / Dev Server",
    8080: "HTTP-Alt"
}

def scan_port(host: str, port: int, timeout: float = 1.0) -> Dict[str, Any]:
    ""${'"'}Probes a single TCP socket port on host.""${'"'}
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.settimeout(timeout)
    start_time = datetime.now()
    try:
        res = s.connect_ex((host, port))
        if res == 0:
            service = COMMON_PORTS.get(port, "Unknown Service")
            return {"port": port, "service": service, "state": "OPEN"}
        else:
            return {"port": port, "state": "CLOSED"}
    except socket.gaierror:
        return {"port": port, "state": "DNS_ERROR"}
    except Exception as e:
        return {"port": port, "state": f"ERROR: {e}"}
    finally:
        s.close()

def scan_target_host(host: str, ports: List[int]) -> List[Dict[str, Any]]:
    print(f"[*] Scanning host '{host}' on {len(ports)} ports...")
    open_ports = []
    for p in ports:
        result = scan_port(host, p)
        if result["state"] == "OPEN":
            print(f"[+] OPEN PORT: {p} ({result.get('service')})")
            open_ports.append(result)
    return open_ports

if __name__ == "__main__":
    target = "127.0.0.1"
    ports_to_test = [22, 80, 443, 8000, 8080]
    open_found = scan_target_host(target, ports_to_test)
    print(f"\nScan finished. Total open ports found on {target}: {len(open_found)}")
""",
            isCustom = false
        ),
        CodeTemplateEntity(
            title = "Web3 Asset SVI Intelligence Engine",
            category = "Web & API",
            description = "FastAPI microservice evaluating Systemic Value Index (SVI), entropy metrics, time decay, and key breaker audits.",
            language = "Python",
            templateCode = """from fastapi import FastAPI, Depends, Body
from pydantic import BaseModel
import asyncio
from datetime import datetime
from typing import Optional, List, Dict, Any

# ========================= 1. INPUT MODELS (Pydantic Validation) =========================
class AddressInput(BaseModel):
    address: str

# ========================= 2. DATA LAYER (The Fetcher Interface) =========================
class DataFetcher:
    ""${'"'}Handles all external I/O operations (Web3, APIs). Purely concerned with retrieving raw facts.""${'"'}
    def __init__(self, web3_provider):
        print("DataFetcher Initialized: Connected to Web3.")
        self.web3 = web3_provider

    async def fetch_live_data(self, address: str) -> dict:
        print(f"Fetching live data for {address}...")
        return {"balance": 120.5, "last_transaction_date": datetime.now()}

    async def get_transaction_history(self, address: str) -> list:
        print(f"Fetching transaction history for {address}...")
        return [{"tx_hash": "xyz", "value": 10}]

# ========================= 3. INTELLIGENCE LAYER (The Brain - Refined Manager) =========================
class IntelligenceEngine:
    ""${'"'}Calculates the Systemic Value Index (SVI) and orchestrates scoring.""${'"'}
    def __init__(self, data_fetcher: DataFetcher):
        self.data_fetcher = data_fetcher
        print("IntelligenceEngine Initialized: Ready to score.")

    def _normalize_balance(self, balance: float) -> float:
        return (balance * 0.5)

    def _calculate_entropy(self, tx_history: list) -> float:
        if not tx_history: return 0.1
        return len(tx_history) / 10.0

    def _calculate_time_decay(self, last_tx_date: datetime, current_dt: datetime) -> float:
        time_diff = (current_dt - last_tx_date).total_seconds() / (60*60*24)
        return max(0.1, 1.0 - (time_diff * 0.1))

    async def calculate_score(self, address: str) -> dict:
        raw_data = await self.data_fetcher.fetch_live_data(address)
        tx_history = await self.data_fetcher.get_transaction_history(address)
        current_dt = datetime.now()

        w1_score = self._normalize_balance(raw_data.get('balance', 0.0))
        w2_score = self._calculate_entropy(tx_history)
        w3_score = self._calculate_time_decay(raw_data['last_transaction_date'], current_dt)

        svi_score = (0.45 * w1_score) + (0.40 * w2_score) + (0.15 * w3_score)
        exploit_result = await self._run_key_breaker(address, svi_score)

        return {
            "score": round(svi_score, 4),
            "key_breaker_result": exploit_result
        }

    async def _run_key_breaker(self, address: str, score: float) -> dict:
        print(f"Executing Key Breaker with Score {score} for {address}")
        return {"status": "OK", "findings": "Key is solid."}

# ========================= 4. PRESENTATION LAYER (FastAPI App) =========================
app = FastAPI(title="SVI Intelligence Engine", version="1.0.0")

class MockWeb3Provider:
    def __init__(self):
        self.endpoint = "https://eth-mainnet.alchemyapi.io/v2/mock"

def get_intelligence_engine(web3_provider = Depends(lambda: MockWeb3Provider())):
    data_fetcher = DataFetcher(web3_provider)
    return IntelligenceEngine(data_fetcher)

@app.get("/analyze_asset/{address}")
async def analyze_endpoint(address: str, engine: IntelligenceEngine = Depends(get_intelligence_engine)):
    try:
        analysis_results = await engine.calculate_score(address)
        return {
            "success": True,
            "score": analysis_results['score'],
            "report": f"Analysis complete. Key Breaker: {analysis_results['key_breaker_result']['findings']}"
        }
    except Exception as e:
        return {
            "success": False,
            "score": None,
            "report": f"FATAL SYSTEM ERROR during analysis: {str(e)}"
        }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
""",
            isCustom = false
        ),
        CodeTemplateEntity(
            title = "Mark-LIV Multimodal Live API Backend & Talking Bridge",
            category = "AI & Voice",
            description = "FatihMakes Mark-LIV Live API talking backend, audio WebSocket PCM streaming, action loader, and memory manager.",
            language = "Python",
            templateCode = """#!/usr/bin/env python3
import asyncio
import json
import os
import sys
import time
from pathlib import Path

# Mark-LIV Backend Core Engine
LIVE_MODEL = "models/gemini-3.1-flash-live-preview"
SEND_SAMPLE_RATE = 16000
RECEIVE_SAMPLE_RATE = 24000

class MarkLivLiveEngine:
    def __init__(self, api_key: str = None):
        self.api_key = api_key or os.getenv("GEMINI_API_KEY", "")
        self.memory = {"facts": ["User preference: Dark mode HUD", "Target device: Samsung Galaxy S-series"]}
        self.actions = ["open_app", "web_search", "browser_control", "system_monitor", "proactive", "screen_process"]
        print(f"[Mark-LIV Engine] Initialized with Live Model '{LIVE_MODEL}'. {len(self.actions)} action plugins registered.")

    def format_system_instruction() -> str:
        return (
            "You are Mark-LIV / A.L.I.S.E., an advanced real-time AI assistant. "
            "You speak clearly, concisely, and with high technical precision."
        )

    async def process_audio_frame(self, pcm_bytes: bytes) -> dict:
        # Simulates 16kHz audio chunk ingestion & EchoGuard filter
        return {"status": "ingested", "bytes": len(pcm_bytes), "sample_rate": SEND_SAMPLE_RATE}

    async def execute_action(self, action_name: str, **kwargs) -> str:
        if action_name not in self.actions:
            return f"Action '{action_name}' unknown."
        return f"[Mark-LIV Action Execution] '{action_name}' executed with args: {kwargs}"

if __name__ == "__main__":
    engine = MarkLivLiveEngine()
    print("=== Mark-LIV Live API Talking Backend Ready ===")
""",
            isCustom = false
        )
    )
}
