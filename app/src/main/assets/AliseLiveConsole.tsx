// src/components/AliseLiveConsole.tsx

import {
  ChangeEvent,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";

export type SessionState =
  | "idle"
  | "connecting"
  | "active"
  | "speaking"
  | "error";

export interface DeviceInfo {
  platform: "android" | "ios" | "windows" | "macos" | "linux" | "unknown";
  deviceType: "phone" | "tablet" | "desktop";
  isMobile: boolean;
  isTouchDevice: boolean;
  isSamsung: boolean;
  isGalaxyS25Family: boolean;
  isGalaxyS26Family: boolean;
  modelHint: string | null;
  browser: string;
  orientation: "portrait" | "landscape";
  screenWidth: number;
  screenHeight: number;
  viewportWidth: number;
  viewportHeight: number;
  devicePixelRatio: number;
  fullscreenSupported: boolean;
  fullscreenActive: boolean;
  standalone: boolean;
  userAgent: string;
}

export interface UseLiveTalkOptions {
  wsUrl: string;
  inputSampleRate?: number;
  outputSampleRate?: number;
  onTextMessage?: (message: string) => void;
  onServerMessage?: (message: ServerMessage) => void;
}

export interface ServerMessage {
  type?: string;
  audio?: string;
  audioData?: string;
  text?: string;
  message?: string;
  [key: string]: unknown;
}

export interface AliseLiveConsoleProps {
  wsUrl: string;
  title?: string;
}

interface FullscreenDocument extends Document {
  webkitFullscreenElement?: Element | null;
  webkitExitFullscreen?: () => Promise<void> | void;
}

interface FullscreenElement extends HTMLElement {
  webkitRequestFullscreen?: () => Promise<void> | void;
}

interface NavigatorWithStandalone extends Navigator {
  standalone?: boolean;
}

interface WindowWithWebkitAudio extends Window {
  webkitAudioContext?: typeof AudioContext;
}

interface WebSocketClientInfoMessage {
  type: "client_info";
  client: {
    app: "ALISE";
    device: DeviceInfo;
    timestamp: string;
  };
}

interface WebSocketAudioMessage {
  type: "audio";
  format: "pcm16";
  sampleRate: number;
  channels: 1;
  data: string;
}

interface WebSocketTextMessage {
  type: "text";
  text: string;
}

const DEFAULT_INPUT_SAMPLE_RATE = 16_000;
const DEFAULT_OUTPUT_SAMPLE_RATE = 24_000;

function floatTo16BitPCM(input: Float32Array): Int16Array {
  const output = new Int16Array(input.length);

  for (let index = 0; index < input.length; index += 1) {
    const sample = Math.max(-1, Math.min(1, input[index]));
    output[index] = sample < 0 ? sample * 0x8000 : sample * 0x7fff;
  }

  return output;
}

function arrayBufferToBase64(buffer: ArrayBuffer): string {
  const bytes = new Uint8Array(buffer);
  const chunkSize = 0x8000;
  let binary = "";

  for (let offset = 0; offset < bytes.length; offset += chunkSize) {
    const chunk = bytes.subarray(offset, offset + chunkSize);

    for (let index = 0; index < chunk.length; index += 1) {
      binary += String.fromCharCode(chunk[index]);
    }
  }

  return window.btoa(binary);
}

function int16ArrayToBase64(input: Int16Array): string {
  const copied = new Uint8Array(input.byteLength);
  copied.set(new Uint8Array(input.buffer, input.byteOffset, input.byteLength));

  return arrayBufferToBase64(copied.buffer);
}

function base64ToFloat32(base64: string): Float32Array {
  const binaryString = window.atob(base64);
  const byteLength = binaryString.length;

  if (byteLength % 2 !== 0) {
    throw new Error("Incoming PCM16 audio contains an invalid byte count.");
  }

  const bytes = new Uint8Array(byteLength);

  for (let index = 0; index < byteLength; index += 1) {
    bytes[index] = binaryString.charCodeAt(index);
  }

  const view = new DataView(bytes.buffer);
  const sampleCount = byteLength / 2;
  const output = new Float32Array(sampleCount);

  for (let index = 0; index < sampleCount; index += 1) {
    const sample = view.getInt16(index * 2, true);
    output[index] = sample < 0 ? sample / 0x8000 : sample / 0x7fff;
  }

  return output;
}

function resampleFloat32(
  input: Float32Array,
  sourceRate: number,
  targetRate: number,
): Float32Array {
  if (sourceRate === targetRate) {
    return input;
  }

  if (input.length === 0) {
    return new Float32Array();
  }

  const ratio = sourceRate / targetRate;
  const outputLength = Math.max(1, Math.round(input.length / ratio));
  const output = new Float32Array(outputLength);

  for (let index = 0; index < outputLength; index += 1) {
    const sourcePosition = index * ratio;
    const leftIndex = Math.floor(sourcePosition);
    const rightIndex = Math.min(leftIndex + 1, input.length - 1);
    const interpolation = sourcePosition - leftIndex;

    output[index] =
      input[leftIndex] * (1 - interpolation) +
      input[rightIndex] * interpolation;
  }

  return output;
}

function getAudioContextConstructor(): typeof AudioContext {
  const browserWindow = window as WindowWithWebkitAudio;
  const Constructor = window.AudioContext ?? browserWindow.webkitAudioContext;

  if (!Constructor) {
    throw new Error("Web Audio API is not supported by this browser.");
  }

  return Constructor;
}

function extractSamsungModel(userAgent: string): string | null {
  const modelMatch = userAgent.match(/\bSM-[A-Z0-9]+\b/i);
  return modelMatch?.[0]?.toUpperCase() ?? null;
}

function detectBrowser(userAgent: string): string {
  if (/SamsungBrowser/i.test(userAgent)) {
    return "Samsung Internet";
  }

  if (/EdgA?/i.test(userAgent)) {
    return "Microsoft Edge";
  }

  if (/CriOS|Chrome/i.test(userAgent)) {
    return "Google Chrome";
  }

  if (/Firefox|FxiOS/i.test(userAgent)) {
    return "Mozilla Firefox";
  }

  if (/Safari/i.test(userAgent) && !/Chrome|CriOS|Android/i.test(userAgent)) {
    return "Safari";
  }

  return "Unknown";
}

function detectPlatform(
  userAgent: string,
): DeviceInfo["platform"] {
  if (/Android/i.test(userAgent)) {
    return "android";
  }

  if (/iPhone|iPad|iPod/i.test(userAgent)) {
    return "ios";
  }

  if (/Windows/i.test(userAgent)) {
    return "windows";
  }

  if (/Macintosh|Mac OS X/i.test(userAgent)) {
    return "macos";
  }

  if (/Linux/i.test(userAgent)) {
    return "linux";
  }

  return "unknown";
}

function isGalaxyS25Model(model: string | null): boolean {
  if (!model) {
    return false;
  }

  return /^SM-S93[1-8][A-Z0-9]*$/i.test(model);
}

function isGalaxyS26Model(model: string | null): boolean {
  if (!model) {
    return false;
  }

  return /^SM-S94[0-9][A-Z0-9]*$/i.test(model);
}

function getFullscreenElement(): Element | null {
  const fullscreenDocument = document as FullscreenDocument;

  return (
    document.fullscreenElement ??
    fullscreenDocument.webkitFullscreenElement ??
    null
  );
}

function supportsFullscreen(): boolean {
  const element = document.documentElement as FullscreenElement;

  return Boolean(
    document.fullscreenEnabled ||
      element.requestFullscreen ||
      element.webkitRequestFullscreen,
  );
}

function getDeviceInfo(): DeviceInfo {
  const userAgent = navigator.userAgent;
  const modelHint = extractSamsungModel(userAgent);
  const platform = detectPlatform(userAgent);

  const isTouchDevice =
    navigator.maxTouchPoints > 0 ||
    "ontouchstart" in window;

  const shortestScreenEdge = Math.min(
    window.screen.width,
    window.screen.height,
  );

  const isMobileUserAgent =
    /Android|iPhone|iPod|Mobile|IEMobile|Opera Mini/i.test(userAgent);

  const isTabletUserAgent =
    /iPad|Tablet/i.test(userAgent) ||
    (/Android/i.test(userAgent) && !/Mobile/i.test(userAgent));

  const isMobile =
    isMobileUserAgent ||
    (isTouchDevice && shortestScreenEdge <= 700);

  let deviceType: DeviceInfo["deviceType"] = "desktop";

  if (isTabletUserAgent || (isTouchDevice && shortestScreenEdge > 700 && isMobile)) {
    deviceType = "tablet";
  } else if (isMobile) {
    deviceType = "phone";
  }

  const isSamsung =
    /Samsung|SM-/i.test(userAgent) ||
    modelHint !== null;

  const navigatorWithStandalone =
    navigator as NavigatorWithStandalone;

  return {
    platform,
    deviceType,
    isMobile,
    isTouchDevice,
    isSamsung,
    isGalaxyS25Family: isGalaxyS25Model(modelHint),
    isGalaxyS26Family: isGalaxyS26Model(modelHint),
    modelHint,
    browser: detectBrowser(userAgent),
    orientation:
      window.innerHeight >= window.innerWidth
        ? "portrait"
        : "landscape",
    screenWidth: window.screen.width,
    screenHeight: window.screen.height,
    viewportWidth: window.innerWidth,
    viewportHeight: window.innerHeight,
    devicePixelRatio: window.devicePixelRatio || 1,
    fullscreenSupported: supportsFullscreen(),
    fullscreenActive: getFullscreenElement() !== null,
    standalone:
      window.matchMedia("(display-mode: standalone)").matches ||
      Boolean(navigatorWithStandalone.standalone),
    userAgent,
  };
}

async function enterFullscreen(element: HTMLElement): Promise<void> {
  const fullscreenElement = element as FullscreenElement;

  if (fullscreenElement.requestFullscreen) {
    await fullscreenElement.requestFullscreen();
    return;
  }

  if (fullscreenElement.webkitRequestFullscreen) {
    await fullscreenElement.webkitRequestFullscreen();
    return;
  }

  throw new Error("Fullscreen mode is not available in this browser.");
}

async function exitFullscreen(): Promise<void> {
  const fullscreenDocument = document as FullscreenDocument;

  if (document.exitFullscreen) {
    await document.exitFullscreen();
    return;
  }

  if (fullscreenDocument.webkitExitFullscreen) {
    await fullscreenDocument.webkitExitFullscreen();
    return;
  }

  throw new Error("The browser cannot exit fullscreen programmatically.");
}

async function copyTextToClipboard(text: string): Promise<void> {
  if (!text) {
    throw new Error("There is no text to copy.");
  }

  if (
    navigator.clipboard &&
    window.isSecureContext
  ) {
    await navigator.clipboard.writeText(text);
    return;
  }

  const textarea = document.createElement("textarea");

  textarea.value = text;
  textarea.setAttribute("readonly", "");
  textarea.style.position = "fixed";
  textarea.style.opacity = "0";
  textarea.style.pointerEvents = "none";

  document.body.appendChild(textarea);
  textarea.select();

  const successful = document.execCommand("copy");
  textarea.remove();

  if (!successful) {
    throw new Error("Clipboard copy is not available.");
  }
}

async function readTextFromClipboard(): Promise<string> {
  if (
    !navigator.clipboard ||
    !window.isSecureContext
  ) {
    throw new Error(
      "Direct paste is blocked here. Long-press the text box and choose Paste.",
    );
  }

  return navigator.clipboard.readText();
}

function serializeDeviceInfo(device: DeviceInfo): string {
  if (device.isGalaxyS25Family) {
    return `Samsung Galaxy S25 family (${device.modelHint ?? "model hidden"})`;
  }

  if (device.isGalaxyS26Family) {
    return `Samsung Galaxy S26 family (${device.modelHint ?? "model hidden"})`;
  }

  if (device.isSamsung && device.deviceType === "phone") {
    return `Samsung Galaxy/Android phone (${device.modelHint ?? "model hidden"})`;
  }

  if (device.deviceType === "phone") {
    return `${device.platform} phone`;
  }

  if (device.deviceType === "tablet") {
    return `${device.platform} tablet`;
  }

  return `${device.platform} desktop`;
}

export function useDeviceInfo(): DeviceInfo {
  const [deviceInfo, setDeviceInfo] =
    useState<DeviceInfo>(() => getDeviceInfo());

  useEffect(() => {
    const updateDeviceInfo = (): void => {
      setDeviceInfo(getDeviceInfo());
    };

    window.addEventListener("resize", updateDeviceInfo);
    window.addEventListener("orientationchange", updateDeviceInfo);
    document.addEventListener("fullscreenchange", updateDeviceInfo);
    document.addEventListener(
      "webkitfullscreenchange",
      updateDeviceInfo as EventListener,
    );

    return () => {
      window.removeEventListener("resize", updateDeviceInfo);
      window.removeEventListener("orientationchange", updateDeviceInfo);
      document.removeEventListener("fullscreenchange", updateDeviceInfo);
      document.removeEventListener(
        "webkitfullscreenchange",
        updateDeviceInfo as EventListener,
      );
    };
  }, []);

  return deviceInfo;
}

export function useLiveTalk({
  wsUrl,
  inputSampleRate = DEFAULT_INPUT_SAMPLE_RATE,
  outputSampleRate = DEFAULT_OUTPUT_SAMPLE_RATE,
  onTextMessage,
  onServerMessage,
}: UseLiveTalkOptions) {
  const [status, setStatus] = useState<SessionState>("idle");
  const [errorMessage, setErrorMessage] =
    useState<string | null>(null);

  const deviceInfo = useDeviceInfo();

  const socketRef = useRef<WebSocket | null>(null);
  const audioContextRef = useRef<AudioContext | null>(null);
  const micStreamRef = useRef<MediaStream | null>(null);
  const micSourceRef =
    useRef<MediaStreamAudioSourceNode | null>(null);
  const processorRef =
    useRef<ScriptProcessorNode | null>(null);

  const nextPlayTimeRef = useRef<number>(0);
  const activeSourcesRef =
    useRef<AudioBufferSourceNode[]>([]);

  const statusRef = useRef<SessionState>("idle");
  const mountedRef = useRef(true);

  const setSessionStatus = useCallback(
    (newStatus: SessionState): void => {
      statusRef.current = newStatus;

      if (mountedRef.current) {
        setStatus(newStatus);
      }
    },
    [],
  );

  const sendJson = useCallback((payload: unknown): boolean => {
    const socket = socketRef.current;

    if (!socket || socket.readyState !== WebSocket.OPEN) {
      return false;
    }

    socket.send(JSON.stringify(payload));
    return true;
  }, []);

  const stopPlayback = useCallback((): void => {
    activeSourcesRef.current.forEach((source) => {
      try {
        source.stop();
      } catch {
        // Source may already have completed.
      }

      try {
        source.disconnect();
      } catch {
        // Source may already be disconnected.
      }
    });

    activeSourcesRef.current = [];

    const context = audioContextRef.current;

    if (context) {
      nextPlayTimeRef.current = context.currentTime;
    }

    if (
      socketRef.current?.readyState === WebSocket.OPEN
    ) {
      socketRef.current.send(
        JSON.stringify({
          type: "barge_in",
        }),
      );
    }

    if (statusRef.current !== "idle") {
      setSessionStatus("active");
    }
  }, [setSessionStatus]);

  const playIncomingAudio = useCallback(
    (base64Data: string): void => {
      const context = audioContextRef.current;

      if (!context) {
        return;
      }

      try {
        const float32Data = base64ToFloat32(base64Data);

        if (float32Data.length === 0) {
          return;
        }

        const audioBuffer = context.createBuffer(
          1,
          float32Data.length,
          outputSampleRate,
        );

        audioBuffer.copyToChannel(float32Data, 0);

        const source = context.createBufferSource();
        source.buffer = audioBuffer;
        source.connect(context.destination);

        const currentTime = context.currentTime;
        const startTime = Math.max(
          nextPlayTimeRef.current,
          currentTime + 0.01,
        );

        source.start(startTime);

        nextPlayTimeRef.current =
          startTime + audioBuffer.duration;

        activeSourcesRef.current.push(source);
        setSessionStatus("speaking");

        source.onended = () => {
          activeSourcesRef.current =
            activeSourcesRef.current.filter(
              (activeSource) => activeSource !== source,
            );

          try {
            source.disconnect();
          } catch {
            // Browser may already have disconnected it.
          }

          if (
            activeSourcesRef.current.length === 0 &&
            socketRef.current?.readyState === WebSocket.OPEN
          ) {
            setSessionStatus("active");
          }
        };
      } catch (error) {
        const message =
          error instanceof Error
            ? error.message
            : "Failed to play incoming audio.";

        setErrorMessage(message);
      }
    },
    [outputSampleRate, setSessionStatus],
  );

  const processServerMessage = useCallback(
    (event: MessageEvent): void => {
      if (typeof event.data !== "string") {
        return;
      }

      let parsed: ServerMessage;

      try {
        parsed = JSON.parse(event.data) as ServerMessage;
      } catch {
        onTextMessage?.(event.data);
        return;
      }

      onServerMessage?.(parsed);

      const audioData =
        typeof parsed.audio === "string"
          ? parsed.audio
          : typeof parsed.audioData === "string"
            ? parsed.audioData
            : null;

      if (audioData) {
        playIncomingAudio(audioData);
      }

      const text =
        typeof parsed.text === "string"
          ? parsed.text
          : typeof parsed.message === "string"
            ? parsed.message
            : null;

      if (text) {
        onTextMessage?.(text);
      }
    },
    [
      onServerMessage,
      onTextMessage,
      playIncomingAudio,
    ],
  );

  const stopMicrophone = useCallback((): void => {
    if (processorRef.current) {
      processorRef.current.onaudioprocess = null;

      try {
        processorRef.current.disconnect();
      } catch {
        // Processor may already be disconnected.
      }

      processorRef.current = null;
    }

    if (micSourceRef.current) {
      try {
        micSourceRef.current.disconnect();
      } catch {
        // Source may already be disconnected.
      }

      micSourceRef.current = null;
    }

    if (micStreamRef.current) {
      micStreamRef.current
        .getTracks()
        .forEach((track) => track.stop());

      micStreamRef.current = null;
    }
  }, []);

  const endSession = useCallback(async (): Promise<void> => {
    stopPlayback();
    stopMicrophone();

    const socket = socketRef.current;

    if (socket) {
      socket.onopen = null;
      socket.onmessage = null;
      socket.onerror = null;
      socket.onclose = null;

      if (
        socket.readyState === WebSocket.OPEN ||
        socket.readyState === WebSocket.CONNECTING
      ) {
        socket.close(1000, "ALISE session ended");
      }

      socketRef.current = null;
    }

    const audioContext = audioContextRef.current;

    if (audioContext && audioContext.state !== "closed") {
      try {
        await audioContext.close();
      } catch {
        // Some mobile browsers close the context during page state changes.
      }
    }

    audioContextRef.current = null;
    nextPlayTimeRef.current = 0;
    activeSourcesRef.current = [];

    setErrorMessage(null);
    setSessionStatus("idle");
  }, [
    setSessionStatus,
    stopMicrophone,
    stopPlayback,
  ]);

  const startMicrophone = useCallback(
    async (
      context: AudioContext,
    ): Promise<void> => {
      if (!navigator.mediaDevices?.getUserMedia) {
        throw new Error(
          "Microphone access is not supported by this browser.",
        );
      }

      const stream =
        await navigator.mediaDevices.getUserMedia({
          audio: {
            channelCount: 1,
            echoCancellation: true,
            noiseSuppression: true,
            autoGainControl: true,
          },
          video: false,
        });

      micStreamRef.current = stream;

      const microphone =
        context.createMediaStreamSource(stream);

      micSourceRef.current = microphone;

      const processor =
        context.createScriptProcessor(4096, 1, 1);

      processorRef.current = processor;

      processor.onaudioprocess = (
        audioEvent: AudioProcessingEvent,
      ): void => {
        const socket = socketRef.current;

        if (
          !socket ||
          socket.readyState !== WebSocket.OPEN
        ) {
          return;
        }

        const rawInput =
          audioEvent.inputBuffer.getChannelData(0);

        const copiedInput = new Float32Array(rawInput);
        const resampledInput = resampleFloat32(
          copiedInput,
          context.sampleRate,
          inputSampleRate,
        );

        const pcm16 =
          floatTo16BitPCM(resampledInput);

        const payload: WebSocketAudioMessage = {
          type: "audio",
          format: "pcm16",
          sampleRate: inputSampleRate,
          channels: 1,
          data: int16ArrayToBase64(pcm16),
        };

        socket.send(JSON.stringify(payload));
      };

      microphone.connect(processor);

      /*
       * ScriptProcessorNode only produces events while connected.
       * The zero-gain destination prevents microphone monitoring.
       */
      const silentGain = context.createGain();
      silentGain.gain.value = 0;

      processor.connect(silentGain);
      silentGain.connect(context.destination);
    },
    [inputSampleRate],
  );

  const startSession = useCallback(async (): Promise<void> => {
    if (
      statusRef.current === "connecting" ||
      statusRef.current === "active" ||
      statusRef.current === "speaking"
    ) {
      return;
    }

    if (!wsUrl.trim()) {
      setErrorMessage("A WebSocket URL is required.");
      setSessionStatus("error");
      return;
    }

    setErrorMessage(null);
    setSessionStatus("connecting");

    try {
      const AudioContextConstructor =
        getAudioContextConstructor();

      const audioContext =
        new AudioContextConstructor();

      audioContextRef.current = audioContext;

      if (audioContext.state === "suspended") {
        await audioContext.resume();
      }

      nextPlayTimeRef.current =
        audioContext.currentTime;

      const socket = new WebSocket(wsUrl);
      socketRef.current = socket;

      socket.onopen = async (): Promise<void> => {
        try {
          const currentDeviceInfo = getDeviceInfo();

          const clientInfoMessage: WebSocketClientInfoMessage = {
            type: "client_info",
            client: {
              app: "ALISE",
              device: currentDeviceInfo,
              timestamp: new Date().toISOString(),
            },
          };

          socket.send(
            JSON.stringify(clientInfoMessage),
          );

          await startMicrophone(audioContext);

          if (
            socketRef.current === socket &&
            socket.readyState === WebSocket.OPEN
          ) {
            setSessionStatus("active");
          }
        } catch (error) {
          const message =
            error instanceof Error
              ? error.message
              : "Could not start the microphone.";

          setErrorMessage(message);
          setSessionStatus("error");
          await endSession();
        }
      };

      socket.onmessage = processServerMessage;

      socket.onerror = (): void => {
        setErrorMessage(
          "The ALISE WebSocket connection encountered an error.",
        );
        setSessionStatus("error");
      };

      socket.onclose = (): void => {
        stopMicrophone();
        stopPlayback();

        if (
          statusRef.current !== "idle" &&
          statusRef.current !== "error"
        ) {
          setSessionStatus("idle");
        }
      };
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "Unable to start ALISE.";

      setErrorMessage(message);
      setSessionStatus("error");

      await endSession();
    }
  }, [
    endSession,
    processServerMessage,
    setSessionStatus,
    startMicrophone,
    stopMicrophone,
    stopPlayback,
    wsUrl,
  ]);

  const sendText = useCallback(
    (text: string): boolean => {
      const normalizedText = text.trim();

      if (!normalizedText) {
        return false;
      }

      const payload: WebSocketTextMessage = {
        type: "text",
        text: normalizedText,
      };

      return sendJson(payload);
    },
    [sendJson],
  );

  useEffect(() => {
    const socket = socketRef.current;

    if (socket?.readyState === WebSocket.OPEN) {
      const payload: WebSocketClientInfoMessage = {
        type: "client_info",
        client: {
          app: "ALISE",
          device: deviceInfo,
          timestamp: new Date().toISOString(),
        },
      };

      socket.send(JSON.stringify(payload));
    }
  }, [deviceInfo]);

  useEffect(() => {
    mountedRef.current = true;

    return () => {
      mountedRef.current = false;

      activeSourcesRef.current.forEach((source) => {
        try {
          source.stop();
        } catch {
          // Cleanup should never block component unmount.
        }
      });

      micStreamRef.current
        ?.getTracks()
        .forEach((track) => track.stop());

      processorRef.current?.disconnect();
      micSourceRef.current?.disconnect();

      socketRef.current?.close();

      const context = audioContextRef.current;

      if (context && context.state !== "closed") {
        void context.close();
      }
    };
  }, []);

  return {
    status,
    errorMessage,
    deviceInfo,
    startSession,
    endSession,
    stopPlayback,
    sendText,
  };
}

export default function AliseLiveConsole({
  wsUrl,
  title = "ALISE",
}: AliseLiveConsoleProps) {
  const [inputText, setInputText] = useState("");
  const [lastReceivedText, setLastReceivedText] =
    useState("");
  const [uiMessage, setUiMessage] =
    useState<string | null>(null);

  const rootRef = useRef<HTMLDivElement | null>(null);

  const {
    status,
    errorMessage,
    deviceInfo,
    startSession,
    endSession,
    stopPlayback,
    sendText,
  } = useLiveTalk({
    wsUrl,
    onTextMessage: setLastReceivedText,
  });

  const deviceLabel = useMemo(
    () => serializeDeviceInfo(deviceInfo),
    [deviceInfo],
  );

  const isConnected =
    status === "active" ||
    status === "speaking";

  const handleInputChange = (
    event: ChangeEvent<HTMLTextAreaElement>,
  ): void => {
    setInputText(event.target.value);
  };

  const handleFullscreen = async (): Promise<void> => {
    setUiMessage(null);

    try {
      if (getFullscreenElement()) {
        await exitFullscreen();
        return;
      }

      if (!rootRef.current) {
        throw new Error(
          "The ALISE display is not ready for fullscreen.",
        );
      }

      await enterFullscreen(rootRef.current);
    } catch (error) {
      setUiMessage(
        error instanceof Error
          ? error.message
          : "Fullscreen operation failed.",
      );
    }
  };

  const handleCopy = async (): Promise<void> => {
    setUiMessage(null);

    const textToCopy =
      inputText || lastReceivedText;

    try {
      await copyTextToClipboard(textToCopy);
      setUiMessage("Copied to clipboard.");
    } catch (error) {
      setUiMessage(
        error instanceof Error
          ? error.message
          : "Copy failed.",
      );
    }
  };

  const handlePaste = async (): Promise<void> => {
    setUiMessage(null);

    try {
      const clipboardText =
        await readTextFromClipboard();

      setInputText((currentText) =>
        currentText
          ? `${currentText}${clipboardText}`
          : clipboardText,
      );

      setUiMessage("Clipboard text pasted.");
    } catch (error) {
      setUiMessage(
        error instanceof Error
          ? error.message
          : "Paste failed.",
      );
    }
  };

  const handleSendText = (): void => {
    if (!inputText.trim()) {
      return;
    }

    if (!isConnected) {
      setUiMessage(
        "Start the ALISE session before sending text.",
      );
      return;
    }

    const sent = sendText(inputText);

    if (sent) {
      setInputText("");
      setUiMessage(null);
    } else {
      setUiMessage(
        "ALISE is not currently connected.",
      );
    }
  };

  const handlePrimarySessionButton =
    async (): Promise<void> => {
      if (
        status === "active" ||
        status === "speaking" ||
        status === "connecting"
      ) {
        await endSession();
      } else {
        await startSession();
      }
    };

  return (
    <div
      ref={rootRef}
      className="alise-live-console"
      data-device-type={deviceInfo.deviceType}
      data-platform={deviceInfo.platform}
      data-samsung={String(deviceInfo.isSamsung)}
      data-galaxy-s25={String(
        deviceInfo.isGalaxyS25Family,
      )}
      data-galaxy-s26={String(
        deviceInfo.isGalaxyS26Family,
      )}
    >
      <style>{`
        .alise-live-console {
          --alise-bg: #020812;
          --alise-panel: #07131f;
          --alise-panel-2: #0a1826;
          --alise-border: #17445e;
          --alise-cyan: #76d4ff;
          --alise-green: #59e889;
          --alise-red: #ff5264;
          --alise-text: #d9e6f2;
          --alise-muted: #718399;

          box-sizing: border-box;
          width: 100%;
          min-height: 100dvh;
          padding:
            max(12px, env(safe-area-inset-top))
            max(12px, env(safe-area-inset-right))
            max(14px, env(safe-area-inset-bottom))
            max(12px, env(safe-area-inset-left));
          background: var(--alise-bg);
          color: var(--alise-text);
          font-family:
            Inter,
            system-ui,
            -apple-system,
            BlinkMacSystemFont,
            "Segoe UI",
            sans-serif;
          overscroll-behavior: none;
        }

        .alise-live-console *,
        .alise-live-console *::before,
        .alise-live-console *::after {
          box-sizing: border-box;
        }

        .alise-shell {
          width: min(100%, 900px);
          margin: 0 auto;
          display: grid;
          gap: 12px;
        }

        .alise-panel {
          border: 1px solid var(--alise-border);
          border-radius: 16px;
          background: linear-gradient(
            180deg,
            rgba(10, 24, 38, 0.96),
            rgba(5, 15, 25, 0.96)
          );
          padding: 14px;
        }

        .alise-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 12px;
        }

        .alise-title {
          display: flex;
          flex-direction: column;
          gap: 4px;
          min-width: 0;
        }

        .alise-title strong {
          color: white;
          font-size: 1rem;
          letter-spacing: 0.08em;
        }

        .alise-device {
          color: var(--alise-cyan);
          font-size: 0.78rem;
          overflow-wrap: anywhere;
        }

        .alise-status-grid {
          display: grid;
          grid-template-columns:
            repeat(3, minmax(0, 1fr));
          gap: 8px;
        }

        .alise-status-card {
          min-width: 0;
          padding: 10px;
          border-radius: 10px;
          border: 1px solid #14344a;
          background: rgba(2, 8, 18, 0.72);
        }

        .alise-label {
          display: block;
          margin-bottom: 4px;
          color: var(--alise-muted);
          font-size: 0.68rem;
          letter-spacing: 0.1em;
          text-transform: uppercase;
        }

        .alise-value {
          color: var(--alise-green);
          font-size: 0.84rem;
          overflow-wrap: anywhere;
        }

        .alise-controls {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
        }

        .alise-button {
          appearance: none;
          min-height: 44px;
          border: 1px solid #27516b;
          border-radius: 10px;
          padding: 8px 13px;
          background: #071522;
          color: var(--alise-text);
          font: inherit;
          font-weight: 600;
          cursor: pointer;
          touch-action: manipulation;
        }

        .alise-button:active {
          transform: translateY(1px);
        }

        .alise-button:disabled {
          opacity: 0.45;
          cursor: not-allowed;
        }

        .alise-button-primary {
          border-color: #5cbbea;
          color: #aee7ff;
        }

        .alise-button-success {
          border-color: #3ba966;
          color: var(--alise-green);
        }

        .alise-button-danger {
          border-color: #d84655;
          color: var(--alise-red);
        }

        .alise-chat {
          display: grid;
          gap: 10px;
        }

        .alise-response {
          min-height: 110px;
          max-height: 34dvh;
          overflow: auto;
          white-space: pre-wrap;
          overflow-wrap: anywhere;
          padding: 12px;
          border: 1px solid #163a51;
          border-radius: 12px;
          background: #06101b;
          line-height: 1.45;
          -webkit-overflow-scrolling: touch;
        }

        .alise-input {
          width: 100%;
          min-height: 110px;
          max-height: 32dvh;
          resize: vertical;
          border: 1px solid #17445e;
          border-radius: 12px;
          padding: 13px;
          background: #06101b;
          color: white;
          font: inherit;
          font-size: 16px;
          line-height: 1.45;
          outline: none;
          caret-color: var(--alise-cyan);
        }

        .alise-input:focus {
          border-color: var(--alise-cyan);
        }

        .alise-message {
          padding: 9px 11px;
          border-radius: 9px;
          color: #bbdcf0;
          background: #0a1a29;
          overflow-wrap: anywhere;
        }

        .alise-error {
          border: 1px solid #812c36;
          color: #ff9ea9;
          background: #270b11;
        }

        .alise-fullscreen {
          width: 100%;
          height: 100%;
          min-height: 100dvh;
        }

        .alise-live-console:fullscreen {
          overflow: auto;
          width: 100vw;
          height: 100dvh;
        }

        .alise-live-console:-webkit-full-screen {
          overflow: auto;
          width: 100vw;
          height: 100dvh;
        }

        @media (max-width: 600px) {
          .alise-live-console {
            padding-left:
              max(8px, env(safe-area-inset-left));
            padding-right:
              max(8px, env(safe-area-inset-right));
          }

          .alise-panel {
            padding: 11px;
            border-radius: 13px;
          }

          .alise-header {
            align-items: flex-start;
            flex-direction: column;
          }

          .alise-status-grid {
            grid-template-columns: 1fr 1fr;
          }

          .alise-controls {
            display: grid;
            grid-template-columns:
              repeat(2, minmax(0, 1fr));
          }

          .alise-button {
            width: 100%;
          }
        }

        @media (display-mode: standalone) {
          .alise-live-console {
            min-height: 100dvh;
          }
        }
      `}</style>

      <main className="alise-shell">
        <section className="alise-panel alise-header">
          <div className="alise-title">
            <strong>{title} LIVE</strong>
            <span className="alise-device">
              {deviceLabel}
            </span>
          </div>

          <button
            type="button"
            className="alise-button alise-button-primary"
            disabled={!deviceInfo.fullscreenSupported}
            onClick={() => {
              void handleFullscreen();
            }}
          >
            {deviceInfo.fullscreenActive
              ? "EXIT FULLSCREEN"
              : "FULLSCREEN"}
          </button>
        </section>

        <section className="alise-panel">
          <div className="alise-status-grid">
            <div className="alise-status-card">
              <span className="alise-label">
                Session
              </span>
              <span className="alise-value">
                {status.toUpperCase()}
              </span>
            </div>

            <div className="alise-status-card">
              <span className="alise-label">
                Device
              </span>
              <span className="alise-value">
                {deviceInfo.deviceType.toUpperCase()}
              </span>
            </div>

            <div className="alise-status-card">
              <span className="alise-label">
                Platform
              </span>
              <span className="alise-value">
                {deviceInfo.platform.toUpperCase()}
              </span>
            </div>

            <div className="alise-status-card">
              <span className="alise-label">
                Browser
              </span>
              <span className="alise-value">
                {deviceInfo.browser}
              </span>
            </div>

            <div className="alise-status-card">
              <span className="alise-label">
                Orientation
              </span>
              <span className="alise-value">
                {deviceInfo.orientation.toUpperCase()}
              </span>
            </div>

            <div className="alise-status-card">
              <span className="alise-label">
                Screen
              </span>
              <span className="alise-value">
                {deviceInfo.screenWidth}
                {" × "}
                {deviceInfo.screenHeight}
              </span>
            </div>
          </div>
        </section>

        <section className="alise-panel">
          <div className="alise-controls">
            <button
              type="button"
              className="alise-button alise-button-success"
              onClick={() => {
                void handlePrimarySessionButton();
              }}
            >
              {isConnected || status === "connecting"
                ? "END SESSION"
                : "START ALISE"}
            </button>

            <button
              type="button"
              className="alise-button alise-button-danger"
              disabled={!isConnected}
              onClick={stopPlayback}
            >
              BARGE IN
            </button>

            <button
              type="button"
              className="alise-button"
              onClick={() => {
                void handleCopy();
              }}
            >
              COPY
            </button>

            <button
              type="button"
              className="alise-button"
              onClick={() => {
                void handlePaste();
              }}
            >
              PASTE
            </button>
          </div>
        </section>

        <section className="alise-panel alise-chat">
          <span className="alise-label">
            ALISE OUTPUT
          </span>

          <div
            className="alise-response"
            aria-live="polite"
          >
            {lastReceivedText ||
              "ALISE output will appear here."}
          </div>

          <label
            className="alise-label"
            htmlFor="alise-live-input"
          >
            MESSAGE / CLIPBOARD INPUT
          </label>

          <textarea
            id="alise-live-input"
            className="alise-input"
            value={inputText}
            onChange={handleInputChange}
            placeholder="Ask ALISE, type, long-press to paste, or use the PASTE button..."
            autoCapitalize="sentences"
            autoCorrect="on"
            spellCheck
            enterKeyHint="send"
          />

          <button
            type="button"
            className="alise-button alise-button-primary"
            disabled={
              !inputText.trim() ||
              !isConnected
            }
            onClick={handleSendText}
          >
            SEND TO ALISE
          </button>
        </section>

        {uiMessage && (
          <div className="alise-message">
            {uiMessage}
          </div>
        )}

        {errorMessage && (
          <div className="alise-message alise-error">
            {errorMessage}
          </div>
        )}

        <section className="alise-panel">
          <span className="alise-label">
            CLIENT ENVIRONMENT SENT TO ALISE
          </span>

          <div className="alise-device">
            {deviceLabel}
            {" • "}
            {deviceInfo.orientation}
            {" • "}
            viewport {deviceInfo.viewportWidth}
            ×
            {deviceInfo.viewportHeight}
            {" • "}
            touch{" "}
            {deviceInfo.isTouchDevice
              ? "yes"
              : "no"}
            {" • "}
            fullscreen{" "}
            {deviceInfo.fullscreenActive
              ? "active"
              : "inactive"}
          </div>
        </section>
      </main>
    </div>
  );
}
