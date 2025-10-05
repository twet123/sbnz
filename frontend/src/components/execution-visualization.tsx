import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Clock, Thermometer } from "lucide-react";
import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

interface ExecutionVisualizationProps {
  data: {
    rulesFired: number;
    events: Array<{
      processId: string | null;
      eventType: "PROCESS_READY" | "PROCESS_SCHEDULED" | "PROCESS_BLOCKED" | "PROCESS_FINISHED" | "END" | "PAGING" | "PREEMPTED" | "IO_RECEIVED";
    }>;
  } | null;
  isLoading: boolean;
}

export function ExecutionVisualization({ data, isLoading }: ExecutionVisualizationProps) {
  const [cpuTemperature, setCpuTemperature] = useState<number | null>(null);
  const [wsConnected, setWsConnected] = useState(false);

  useEffect(() => {
    const socketUrl = "http://localhost:8080/ws";
    const client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      reconnectDelay: 5000,
      onConnect: () => {
        setWsConnected(true);
        console.log("connected to ws");
        client.subscribe("/temperature", (message) => {
          console.log(message);
          try {
            const data = JSON.parse(message.body);
            if (typeof data.temperature === "number") {
              setCpuTemperature(data.temperature);
            }
          } catch (error) {
            console.error("Error parsing STOMP message:", error);
          }
        });
      },
      onStompError: (frame) => {
        setWsConnected(false);
        console.error("Broker error:", frame.headers["message"]);
      },
      onWebSocketClose: () => setWsConnected(false),
      onWebSocketError: () => setWsConnected(false),
    });

    return () => {
      client.deactivate();
    };
  }, []);

  if (isLoading) {
    return (
      <Card className="border-border bg-card">
        <CardHeader>
          <CardTitle className="text-foreground">Execution Timeline</CardTitle>
          <CardDescription className="text-muted-foreground">Processing scheduler simulation...</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center py-12">
            <div className="flex flex-col items-center gap-4">
              <div className="h-12 w-12 animate-spin rounded-full border-4 border-primary border-t-transparent" />
              <p className="text-sm text-muted-foreground">Running simulation...</p>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (!data) {
    return (
      <Card className="border-border bg-card">
        <CardHeader>
          <CardTitle className="text-foreground">Execution Timeline</CardTitle>
          <CardDescription className="text-muted-foreground">Configure processes and run the scheduler to see results</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center py-12">
            <div className="flex flex-col items-center gap-4 text-center">
              <div className="flex h-16 w-16 items-center justify-center rounded-full bg-muted">
                <Clock className="h-8 w-8 text-muted-foreground" />
              </div>
              <div>
                <p className="font-medium text-foreground">No simulation data</p>
                <p className="text-sm text-muted-foreground mt-1">Add processes and click "Run Scheduler" to begin</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  // Example visualization - adapt based on actual API response structure
  const events = data.events || [];
  const totalTime = data.rulesFired || 0;

  const getEventBadgeVariant = (eventType: string) => {
    switch (eventType) {
      case "PROCESS_READY":
        return "bg-blue-500/20 text-blue-400 border-blue-500/30";
      case "PROCESS_SCHEDULED":
        return "bg-green-500/20 text-green-400 border-green-500/30";
      case "PROCESS_BLOCKED":
        return "bg-yellow-500/20 text-yellow-400 border-yellow-500/30";
      case "PROCESS_FINISHED":
        return "bg-purple-500/20 text-purple-400 border-purple-500/30";
      case "END":
        return "bg-red-500/20 text-red-400 border-red-500/30";
      default:
        return "bg-gray-500/20 text-gray-400 border-gray-500/30";
    }
  };

  const formatEventType = (eventType: string) => {
    return eventType
      .split("_")
      .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
      .join(" ");
  };

  const getTemperatureColor = () => {
    if (cpuTemperature === null) return "text-foreground";
    if (cpuTemperature >= 80) return "text-red-400";
    if (cpuTemperature >= 60) return "text-yellow-400";
    return "text-green-400";
  };

  return (
    <div className="space-y-6">
      <Card className="border-border bg-card">
        <CardHeader>
          <CardTitle className="text-foreground">System Monitor</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-3 rounded-lg border border-border bg-secondary/30 p-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-chart-2/10 border border-chart-2/20">
              <Thermometer className="h-5 w-5 text-chart-2" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground">CPU Temperature</p>
              <p className={`text-lg font-semibold ${getTemperatureColor()}`}>
                {cpuTemperature !== null ? `${cpuTemperature}°C` : !wsConnected ? "Not connected" : "Waiting..."}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card className="border-border bg-card">
        <CardHeader>
          <CardTitle className="text-foreground">Execution Summary</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="flex items-center gap-3 rounded-lg border border-border bg-secondary/30 p-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-chart-1/10 border border-chart-1/20">
                <Clock className="h-5 w-5 text-chart-1" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground text-left">Total Time</p>
                <p className="text-lg font-semibold text-foreground text-left">{totalTime} cycles</p>
              </div>
            </div>
            <div className="flex items-center gap-3 rounded-lg border border-border bg-secondary/30 p-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-chart-2/10 border border-chart-2/20">
                <Thermometer className="h-5 w-5 text-chart-2" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground text-left">CPU Temperature</p>
                <p className={`text-lg font-semibold text-left ${getTemperatureColor()}`}>
                  {cpuTemperature !== null ? `${cpuTemperature}°C` : !wsConnected ? "Not connected" : "Waiting..."}
                </p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card className="border-border bg-card">
        <CardHeader>
          <CardTitle className="text-foreground">Execution Events</CardTitle>
          <CardDescription className="text-muted-foreground">Timeline of process state changes during execution</CardDescription>
        </CardHeader>
        <CardContent>
          <ScrollArea className="h-[500px] pr-4">
            <div className="space-y-3">
              {events.length === 0 ? (
                <p className="text-center text-sm text-muted-foreground py-8">No execution events available</p>
              ) : (
                events.map((event, index) => (
                  <div key={index} className="rounded-lg border border-border bg-secondary/30 p-4">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <Badge variant="outline" className="border-primary/30 bg-primary/10 text-primary">
                            Event {index + 1}
                          </Badge>
                          {event.processId && (
                            <Badge variant="outline" className="border-border text-muted-foreground">
                              Process {event.processId}
                            </Badge>
                          )}
                        </div>
                        <div className="flex items-center gap-2">
                          <Badge className={getEventBadgeVariant(event.eventType)}>{formatEventType(event.eventType)}</Badge>
                        </div>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </ScrollArea>
        </CardContent>
      </Card>
    </div>
  );
}
