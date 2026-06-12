import { serve } from "https://deno.land/std@0.168.0/http/server.ts"

const REVENUECAT_API_KEY = Deno.env.get('REVENUECAT_API_KEY')
const REVENUECAT_PROJECT_ID = Deno.env.get('REVENUECAT_PROJECT_ID')

serve(async (req) => {
  const corsHeaders = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'POST, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization, apikey, X-Client-Info',
  }

  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const { appUserId, adjustments } = await req.json()

    if (!appUserId || !adjustments) {
      return new Response(JSON.stringify({ error: 'Missing appUserId or adjustments' }), {
        status: 400,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' }
      })
    }

    if (!REVENUECAT_PROJECT_ID) {
        return new Response(JSON.stringify({ error: 'REVENUECAT_PROJECT_ID secret not set in Supabase' }), {
            status: 500,
            headers: { ...corsHeaders, 'Content-Type': 'application/json' }
        })
    }

    const encodedUserId = encodeURIComponent(appUserId)
    const rcUrl = `https://api.revenuecat.com/v2/projects/${REVENUECAT_PROJECT_ID}/customers/${encodedUserId}/virtual_currencies/transactions`

    console.log(`Adjusting economy for user: ${appUserId}. Adjustments:`, adjustments)

    const response = await fetch(rcUrl, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${REVENUECAT_API_KEY}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        adjustments: adjustments
      })
    })

    const data = await response.json()

    if (!response.ok) {
      console.error('RevenueCat Error:', response.status, data)
      return new Response(JSON.stringify({
        error: 'RevenueCat API Error',
        status: response.status,
        details: data
      }), {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: response.status
      })
    }

    return new Response(JSON.stringify(data), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      status: 200,
    })
  } catch (error) {
    console.error('Internal Error:', error)
    return new Response(JSON.stringify({ error: error.message }), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      status: 500
    })
  }
})
