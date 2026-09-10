{{- define "reminder-api.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "reminder-api.labels" -}}
app.kubernetes.io/name: {{ include "reminder-api.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Values.image.tag | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "reminder-api.selectorLabels" -}}
app.kubernetes.io/name: {{ include "reminder-api.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}
