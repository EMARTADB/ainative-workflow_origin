import { type Plugin, tool } from "@opencode-ai/plugin"

export const SessionTitlePlugin: Plugin = async ({ client }) => {
  return {
    tool: {
      set_session_title: tool({
        description:
          "Rename the current OpenCode session. Use this when the user asks to change the current session title/name.",
        args: {
          title: tool.schema
            .string()
            .min(1)
            .max(80)
            .describe("New short title for the current session"),
        },

        async execute(args, context) {
          const timestamp = new Intl.DateTimeFormat("sv-SE", {
            timeZone: "Europe/Berlin",
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit",
            hour12: false,
          }).format(new Date())

          const titleWithPrefix = `${timestamp} - ${args.title}`

          const result = await client.session.update({
            path: { id: context.sessionID },
            body: { title: titleWithPrefix },
          })

          const session = result.data

          return JSON.stringify(
            {
              opencode_session_name: session.title,
              opencode_session_id: session.id,
            },
            null,
            2,
          )
        }

      }),
    },
  }
}
