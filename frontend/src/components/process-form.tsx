import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Plus, Trash2, Send } from "lucide-react";

interface Process {
  id: string;
  priority: number;
  memoryRequirement: number;
  safeMemoryLimit: number;
  instructions: number;
  ioInstructions: number[];
}

interface ProcessFormProps {
  onSubmit: (data: any) => void;
  isLoading: boolean;
}

export function ProcessForm({ onSubmit, isLoading }: ProcessFormProps) {
  const [processes, setProcesses] = useState<Process[]>([]);
  const [cpuCores, setCpuCores] = useState(4);
  const [totalMemory, setTotalMemory] = useState(8192);

  const [currentProcess, setCurrentProcess] = useState({
    priority: 1,
    memoryRequirement: 0,
    safeMemoryLimit: 0,
    instructions: 0,
    ioInstructions: "",
  });

  const addProcess = () => {
    if (currentProcess.instructions <= 0) return;

    const ioArray = currentProcess.ioInstructions
      .split(",")
      .map((s) => Number.parseInt(s.trim()))
      .filter((n) => !isNaN(n) && n > 0 && n <= currentProcess.instructions);

    const newProcess: Process = {
      id: (processes.length + 1).toString(),
      priority: currentProcess.priority,
      memoryRequirement: currentProcess.memoryRequirement,
      safeMemoryLimit: currentProcess.safeMemoryLimit,
      instructions: currentProcess.instructions,
      ioInstructions: ioArray,
    };

    setProcesses([...processes, newProcess]);
    setCurrentProcess({
      priority: 1,
      memoryRequirement: 0,
      safeMemoryLimit: 0,
      instructions: 0,
      ioInstructions: "",
    });
  };

  const removeProcess = (id: String) => {
    setProcesses(processes.filter((p) => p.id !== id));
  };

  const handleSubmit = () => {
    const data = {
      processes,
      system: {
        cpuCores,
        totalMemory,
      },
    };

    onSubmit(data);
  };

  return (
    <div className="space-y-6">
      <Card className="border-border bg-card">
        <CardHeader>
          <CardTitle className="text-foreground text-left">System Configuration</CardTitle>
          <CardDescription className="text-muted-foreground text-left">Define the hardware resources available</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="cpuCores" className="text-foreground">
                CPU Cores
              </Label>
              <Input
                id="cpuCores"
                type="number"
                min={1}
                max={32}
                value={cpuCores}
                onChange={(e) => setCpuCores(Number.parseInt(e.target.value) || 1)}
                className="bg-input border:border text-foreground"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="totalMemory" className="text-foreground">
                Total Memory (MB)
              </Label>
              <Input
                id="totalMemory"
                type="number"
                min={512}
                value={totalMemory}
                onChange={(e) => setTotalMemory(Number.parseInt(e.target.value) || 512)}
                className="bg-input border:border text-foreground"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card className="border-border bg-card">
        <CardHeader>
          <CardTitle className="text-foreground text-left">Add Process</CardTitle>
          <CardDescription className="text-muted-foreground text-left">Define a new process with its requirements</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="priority" className="text-foreground">
                Priority
              </Label>
              <Input
                id="priority"
                type="number"
                min="1"
                value={currentProcess.priority}
                onChange={(e) => setCurrentProcess({ ...currentProcess, priority: Number.parseInt(e.target.value) || 1 })}
                className="bg-input border-border text-foreground"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="instructions" className="text-foreground">
                Instructions
              </Label>
              <Input
                id="instructions"
                type="number"
                min="1"
                value={currentProcess.instructions || ""}
                onChange={(e) => setCurrentProcess({ ...currentProcess, instructions: Number.parseInt(e.target.value) || 0 })}
                className="bg-input border-border text-foreground"
              />
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="memory" className="text-foreground">
                Memory Required (MB)
              </Label>
              <Input
                id="memory"
                type="number"
                min="0"
                value={currentProcess.memoryRequirement || ""}
                onChange={(e) => setCurrentProcess({ ...currentProcess, memoryRequirement: Number.parseInt(e.target.value) || 0 })}
                className="bg-input border-border text-foreground"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="safeMemory" className="text-foreground">
                Safe Memory Limit (MB)
              </Label>
              <Input
                id="safeMemory"
                type="number"
                min="0"
                value={currentProcess.safeMemoryLimit || ""}
                onChange={(e) => setCurrentProcess({ ...currentProcess, safeMemoryLimit: Number.parseInt(e.target.value) || 0 })}
                className="bg-input border-border text-foreground"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="ioInstructions" className="text-foreground">
              IO Instructions (comma-separated)
            </Label>
            <Input
              id="ioInstructions"
              placeholder="e.g., 3, 7, 12"
              value={currentProcess.ioInstructions}
              onChange={(e) => setCurrentProcess({ ...currentProcess, ioInstructions: e.target.value })}
              className="bg-input border-border text-foreground font-mono"
            />
            <p className="text-xs text-muted-foreground">Specify which instruction numbers are IO operations</p>
          </div>

          <Button
            onClick={addProcess}
            className="w-full bg-primary text-primary-foreground hover:bg-primary/90 cursor-pointer"
            disabled={currentProcess.instructions <= 0}
          >
            <Plus className="mr-2 h-4 w-4" />
            Add Process
          </Button>
        </CardContent>
      </Card>

      {processes.length > 0 && (
        <Card className="border-border bg-card">
          <CardHeader>
            <CardTitle className="text-foreground text-left">
              Processes Queue
              <Badge variant="secondary" className="ml-2 bg-primary/10 text-primary border-primary/20">
                {processes.length}
              </Badge>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {processes.map((process) => (
                <div key={process.id} className="flex items-center justify-between rounded-lg border border-border bg-secondary/50 p-3">
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="font-medium text-foreground">ID: {process.id}</span>
                      <Badge variant="outline" className="text-xs border-border text-muted-foreground">
                        P{process.priority}
                      </Badge>
                    </div>
                    <div className="mt-1 flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
                      <span>{process.instructions} instructions</span>
                      <span>{process.memoryRequirement}MB</span>
                      {process.ioInstructions.length > 0 && <span>IO: {process.ioInstructions.join(", ")}</span>}
                    </div>
                  </div>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => removeProcess(process.id)}
                    className="text-muted-foreground hover:text-destructive hover:bg-destructive/10"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      <Button
        onClick={handleSubmit}
        disabled={processes.length === 0 || isLoading}
        className="w-full bg-accent text-accent-foreground hover:bg-accent/90 cursor-pointer"
        size="lg"
      >
        {isLoading ? (
          <>Processing...</>
        ) : (
          <>
            <Send className="mr-2 h-4 w-4" />
            Run Scheduler
          </>
        )}
      </Button>
    </div>
  );
}
