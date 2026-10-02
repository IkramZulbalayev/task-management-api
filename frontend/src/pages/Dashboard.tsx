import { useQuery } from "@tanstack/react-query";
import Layout from "../components/Layout";
import { Card, ErrorBanner } from "../components/ui";
import { getDashboard } from "../api/dashboard";
import { getErrorMessage } from "../api/client";
import { formatStatus } from "../utils/format";

export default function Dashboard() {
  const { data: stats, error, isLoading } = useQuery({
    queryKey: ["dashboard"],
    queryFn: getDashboard,
  });

  const cards: { label: string; value?: number; warn?: boolean }[] = [
    { label: "Total Projects", value: stats?.totalProjects },
    { label: "Total Tasks", value: stats?.totalTasks },
    { label: "Completed", value: stats?.completedTasks },
    { label: "In Progress", value: stats?.inProgressTasks },
    { label: "Overdue", value: stats?.overdueTasks, warn: true },
  ];

  return (
    <Layout>
      <h1 className="page-title">Dashboard</h1>
      <ErrorBanner message={error ? getErrorMessage(error, "Could not load dashboard data.") : null} />
      {isLoading && <p>Loading...</p>}

      {stats && (
        <>
          <div className="stat-grid">
            {cards.map((c) => (
              <Card key={c.label}>
                <div className="stat-label">{c.label}</div>
                <div className={`stat-value ${c.warn ? "stat-warning" : ""}`}>{c.value ?? "—"}</div>
              </Card>
            ))}
          </div>

          {stats.tasksByStatus && (
            <Card className="chart-card">
              <div className="stat-label">Tasks by Status</div>
              <div className="bar-chart">
                {Object.entries(stats.tasksByStatus).map(([status, count]) => (
                  <div className="bar-row" key={status}>
                    <span className="bar-label">{formatStatus(status)}</span>
                    <div className="bar-track">
                      <div
                        className="bar-fill"
                        style={{
                          width: `${Math.min(100, (count / (stats.totalTasks || 1)) * 100)}%`,
                        }}
                      />
                    </div>
                    <span className="bar-count">{count}</span>
                  </div>
                ))}
              </div>
            </Card>
          )}
        </>
      )}
    </Layout>
  );
}
