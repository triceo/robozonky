<p>Rezervace půjčky <@idLoan data=data /> byla potvrzena.</p>

<table style="width: 60%;">
  <tr>
    <th style="width: 20%; text-align: right;">Investovaná částka:</th>
    <td>${data.amountHeld?string.currency}</td>
  </tr>
</table>

<#include "additional-loan-info.ftl">

<#include "additional-portfolio-info.ftl">
