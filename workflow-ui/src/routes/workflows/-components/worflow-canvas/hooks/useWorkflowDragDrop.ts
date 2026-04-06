import { Node, useReactFlow } from "@xyflow/react";
import { useCallback } from "react";
import { humanId } from "human-id";
import { Plugin, PluginMetadataMap } from "@/types/plugins";

type UseWorkflowDragDropProps = {
    setNodes: (nodes: Node[] | ((nodes: Node[]) => Node[])) => void;
};

type UseWorkflowDragDropReturn = {
    onDragOver: (event: React.DragEvent) => void;
    onDrop: (event: React.DragEvent) => void;
};

/**
 * Hook to manage workflow node drag and drop operations
 * Handles node creation and placement
 */
export const useWorkflowDragDrop = ({ setNodes }: UseWorkflowDragDropProps): UseWorkflowDragDropReturn => {
    const { screenToFlowPosition } = useReactFlow();

    // Create a new node with given type and position
    const createNode = useCallback((type: Plugin, position: { x: number; y: number }): Node => {
        return {
            id: `${type}_${humanId({ separator: "-", capitalize: false })}`,
            type,
            position,
            data: {
                label: type,
                icon: PluginMetadataMap[type].icon,
            },
        };
    }, []);

    // Handle drag over event
    const onDragOver = useCallback((event: React.DragEvent) => {
        event.preventDefault();
        event.dataTransfer.dropEffect = "move";
    }, []);

    // Handle drop event
    const onDrop = useCallback(
        (event: React.DragEvent) => {
            event.preventDefault();

            const type = event.dataTransfer.getData("application/@xyflow/react") as Plugin;
            if (!type) return;

            const position = screenToFlowPosition({
                x: event.clientX,
                y: event.clientY,
            });

            const newNode = createNode(type, position);
            setNodes((nds) => nds.concat(newNode));
        },
        [screenToFlowPosition, setNodes, createNode]
    );

    return {
        onDragOver,
        onDrop,
    };
}; 