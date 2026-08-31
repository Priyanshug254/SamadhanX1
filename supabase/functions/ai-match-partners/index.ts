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

    // Fetch challenge domain and capabilities
    const { data: challenge } = await supabase
      .from("challenges")
      .select("id, domain_id, title, description, district, state")
      .eq("id", challenge_id)
      .single();

    if (!challenge) {
      return new Response(JSON.stringify({ error: "Challenge not found" }), {
        status: 404,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Find verified organizations with matching capabilities
    const { data: capabilities } = await supabase
      .from("partner_capabilities")
      .select("*, organizations(id, name, type, verification_status, state)")
      .eq("is_active", true)
      .eq("domain_id", challenge.domain_id);

    const matches = (capabilities || []).map((cap: any) => {
      let score = 70;
      const reasons = [`Domain capability match: ${cap.capability_type}`];
      if (cap.organizations?.state === challenge.state) {
        score += 20;
        reasons.push(`Regional proximity match (${challenge.state})`);
      }
      if (cap.organizations?.verification_status === "VERIFIED") {
        score += 10;
        reasons.push("Fully Verified Partner Organization");
      }

      return {
        organization_id: cap.organizations?.id,
        organization_name: cap.organizations?.name,
        organization_type: cap.organizations?.type,
        match_score: score,
        match_reasons: reasons,
      };
    });

    return new Response(
      JSON.stringify({
        success: true,
        data: {
          challenge_id,
          matched_partners: matches,
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
