import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return new Response(JSON.stringify({ error: "Missing authorization header" }), {
        status: 401,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
    const supabase = createClient(supabaseUrl, supabaseAnonKey, {
      global: { headers: { Authorization: authHeader } },
    });

    const { data: { user }, error: authError } = await supabase.auth.getUser();
    if (authError || !user) {
      return new Response(JSON.stringify({ error: "Unauthorized user" }), {
        status: 401,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const { challenge_id } = await req.json();
    if (!challenge_id) {
      return new Response(JSON.stringify({ error: "challenge_id is required" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Fetch challenge details
    const { data: challenge, error: challengeError } = await supabase
      .from("challenges")
      .select("*, domains(name)")
      .eq("id", challenge_id)
      .single();

    if (challengeError || !challenge) {
      return new Response(JSON.stringify({ error: "Challenge not found" }), {
        status: 404,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const geminiKey = Deno.env.get("GEMINI_API_KEY");
    let summary = "";
    let actionItems = [];

    if (geminiKey) {
      const prompt = `Summarize this societal challenge for government officials and technical partners:
Title: ${challenge.title}
Domain: ${challenge.domains?.name || 'General'}
Location: ${challenge.location} (${challenge.district || ''}, ${challenge.state || ''})
Description: ${challenge.description}
Affected Population: ${challenge.affected_population || 'Not specified'}

Respond strictly with valid JSON in this format:
{
  "executive_summary": "...",
  "recommended_action_items": ["item1", "item2", "item3"],
  "estimated_technical_complexity": "LOW | MEDIUM | HIGH"
}`;

      const geminiRes = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${geminiKey}`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            contents: [{ parts: [{ text: prompt }] }],
            generationConfig: { responseMimeType: "application/json" },
          }),
        }
      );

      const geminiData = await geminiRes.json();
      const rawText = geminiData.candidates?.[0]?.content?.parts?.[0]?.text;
      const parsed = JSON.parse(rawText || "{}");
      summary = parsed.executive_summary || challenge.description;
      actionItems = parsed.recommended_action_items || [];
    } else {
      summary = `Citizen-reported issue regarding ${challenge.title} at ${challenge.location}. Requires departmental review and pilot intervention.`;
      actionItems = [
        "Conduct on-site inspection",
        "Verify affected population impact",
        "Route to regional technical innovation partner"
      ];
    }

    // Update challenge summary in database
    await supabase
      .from("challenges")
      .update({ ai_summary: summary })
      .eq("id", challenge_id);

    return new Response(
      JSON.stringify({
        success: true,
        data: {
          challenge_id,
          summary,
          action_items: actionItems,
        },
      }),
      {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      }
    );
  } catch (err: any) {
    return new Response(JSON.stringify({ error: err.message || "Internal server error" }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
