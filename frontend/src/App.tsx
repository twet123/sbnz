import { Cpu } from "lucide-react";
import "./App.css";
import { ProcessForm } from "@/components/process-form";
import { useState } from "react";

function App() {
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (data: any) => {
    console.log(data);
  };

  return (
    <main className="min-h-screen bg-background p-6 md:p-8">
      <div className="mx-auto max-w-7xl space-y-8">
        <header className="flex items-center gap-3 border-b border-border pb-6">
          <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10 border border-primary/20">
            <Cpu className="h-6 w-6 text-primary" />
          </div>
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-foreground text-left">Process Scheduler Simulator</h1>
            <p className="text-sm text-muted-foreground mt-1">Configure processes and system resources to visualize CPU scheduling</p>
          </div>
        </header>

        <div className="grid gap-8 lg:grid-cols-2">
          <ProcessForm onSubmit={handleSubmit} isLoading={isLoading} />
          {/* <ExecutionVisualization data={executionSteps} isLoading={isLoading} /> */}
        </div>
      </div>
    </main>
  );
}

export default App;
