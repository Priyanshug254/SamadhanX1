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

    const { title, description } = await req.json();
    if (!title || !description) {
      return new Response(JSON.stringify({ error: "Title and description are required" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const geminiKey = Deno.env.get("GEMINI_API_KEY");

    let result;
    if (geminiKey) {
      const prompt = `Analyze this citizen reported societal challenge:
Title: ${title}
Description: ${description}

Classify into one of these domain codes:
WATER_SANITATION, AGRI_TECH, CLEAN_ENERGY, HEALTHCARE, URBAN_MOBILITY, WASTE_MGMT, DISASTER_RESILIENCE, EDUCATION_SKILLING.
Provide severity (LOW, MEDIUM, HIGH, CRITICAL), urgency (LOW, MEDIUM, HIGH, IMMEDIATE), key category tags, and a 2-sentence summary.
Respond strictly with valid JSON in this format:
{
  "recommended_domain_code": "...",
  "severity": "...",
  "urgency": "...",
  "category_tags": ["tag1", "tag2"],
  "summary": "..."
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
      result = JSON.parse(rawText || "{}");
    } else {
      // Deterministic classification fallback
      const text = `${title} ${description}`.toLowerCase();
      let domain = "WATER_SANITATION";
      if (text.includes("road") || text.includes("traffic") || text.includes("pothole")) domain = "URBAN_MOBILITY";
      else if (text.includes("crop") || text.includes("farm") || text.includes("agriculture")) domain = "AGRI_TECH";
      else if (text.includes("solar") || text.includes("electricity") || text.includes("power")) domain = "CLEAN_ENERGY";
      else if (text.includes("hospital") || text.includes("health") || text.includes("disease")) domain = "HEALTHCARE";
      else if (text.includes("waste") || text.includes("garbage") || text.includes("plastic")) domain = "WASTE_MGMT";
      else if (text.includes("flood") || text.includes("earthquake") || text.includes("rescue")) domain = "DISASTER_RESILIENCE";
      else if (text.includes("school") || text.includes("student") || text.includes("skill")) domain = "EDUCATION_SKILLING";

      result = {
        recommended_domain_code: domain,
        severity: text.includes("urgent") || text.includes("critical") ? "CRITICAL" : "HIGH",
        urgency: text.includes("immediately") ? "IMMEDIATE" : "HIGH",
        category_tags: [domain.toLowerCase(), "civic_issue"],
        summary: description.slice(0, 150) + "...",
      };
    }

    return new Response(JSON.stringify({ success: true, data: result }), {
      status: 200,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (err: any) {
    return new Response(JSON.stringify({ error: err.message || "Internal server error" }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
